import React, { useState, useEffect, useCallback, useRef, useMemo } from 'react';
import EditorJsWrapper from './EditorJsWrapper';
import  EditorJS, { OutputData, BlockToolData, API as EditorJSAPI } from '@editorjs/editorjs';
import { debounce } from 'lodash-es';

import { SuperObject, ContentBlock, EditorJsBlockData } from '../../../shared/api/models';
import {
  getSuperObjectByMongoId,
  createSuperObject,
  syncDocumentBlocksApi,
  getAllContentBlocksForSuperObject,
  uploadImageApi
} from '../../../shared/api/noteApi';

import './NoteEditorWidget.scss';

interface NoteEditorWidgetProps {
  noteId?: string; 
  readOnly?: boolean;
}

interface ImageUploadResult {
  success: 1 | 0;
  file: { url: string; name?: string; size?: number; [key: string]: any; };
  message?: string;
}

const convertContentBlocksToEditorJsData = (contentBlocksFromDb: ContentBlock[]): OutputData => {
  if (!contentBlocksFromDb || contentBlocksFromDb.length === 0) {
    return { time: Date.now(), blocks: [{ type: 'paragraph', data: { text: '' } }], version: "2.30.0" };
  }
  return {
    time: Date.now(),
    blocks: contentBlocksFromDb.map(dbBlock => {
      let blockTypeForEditor = dbBlock.objectType;
      if (blockTypeForEditor === 'unknown_tool_error' || !blockTypeForEditor) {
        blockTypeForEditor = 'paragraph';
      }
      let blockDataForEditor = dbBlock.data || {};
      if (blockTypeForEditor === 'paragraph' && typeof blockDataForEditor.text !== 'string') {
        blockDataForEditor = { text: '' };
      }
      return {
        id: dbBlock.id, 
        type: blockTypeForEditor,
        data: blockDataForEditor,
      };
    }),
    version: "2.30.0",
  };
};

const convertEditorJsBlocksToPayload = (editorJsBlocks: BlockToolData[]): EditorJsBlockData[] => {
  return editorJsBlocks.map((ejBlock) => {
    const blockType = (ejBlock as any).type || (ejBlock as any).tool;
    if (!blockType) {
      console.error(`[CONVERT PAYLOAD] Блок ОТ Editor.js НЕ ИМЕЕТ 'type' или 'tool'! Блок:`, ejBlock);
      return {
        id: ejBlock.id,
        type: 'unknown_type_error_client',
        data: ejBlock.data || {},
      };
    }
    return {
      id: ejBlock.id,
      type: blockType as string,
      data: ejBlock.data,
    };
  });
};


export const NoteEditorWidget: React.FC<NoteEditorWidgetProps> = ({ noteId, readOnly = false }) => {
  const [currentSuperObject, setCurrentSuperObject] = useState<SuperObject | null>(null);
  const [editorInitialDataUsed, setEditorInitialDataUsed] = useState<OutputData | undefined>(undefined);
  
  const [isLoading, setIsLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);
  
  const latestEditorDataRef = useRef<OutputData | null>(null);
  const currentSuperObjectRefForDebounce = useRef<SuperObject | null>(null);
  const loadingFileIdRef = useRef<number | null>(null);

  const editorJsInstanceApiRef = useRef<EditorJSAPI | null>(null);
  const editorJsInstanceRef = useRef<EditorJS | null>(null);


  useEffect(() => {
    currentSuperObjectRefForDebounce.current = currentSuperObject;
  }, [currentSuperObject]);


  useEffect(() => {
    if (!noteId) {
      setIsLoading(false); 
      setError('ID заметки не предоставлен.'); 
      setEditorInitialDataUsed(undefined); 
      setCurrentSuperObject(null); 
      loadingFileIdRef.current = null; 
      editorJsInstanceApiRef.current = null;
      return;
    }
    const fileId = parseInt(noteId, 10);
    if (isNaN(fileId)) {
      setIsLoading(false); 
      setError('Некорректный ID заметки.');
      setEditorInitialDataUsed(undefined); 
      setCurrentSuperObject(null); 
      loadingFileIdRef.current = null; 
      editorJsInstanceApiRef.current = null;
      return;
    }

    if (loadingFileIdRef.current === fileId && isLoading) {
        return;
    }

    if (!currentSuperObject || currentSuperObject.fileId !== fileId) {
        loadingFileIdRef.current = fileId;
        setIsLoading(true);
        setError(null);
        setCurrentSuperObject(null);
        setEditorInitialDataUsed(undefined);
        latestEditorDataRef.current = null;
        editorJsInstanceApiRef.current = null;
    } else if (isLoading && loadingFileIdRef.current === fileId) {
        console.log(`[LOAD EFFECT] Загрузка для fileId ${fileId} уже в процессе (isLoading=true). Пропускаем.`);
        return;
    } else if (!isLoading && currentSuperObject && currentSuperObject.fileId === fileId) {
        console.log(`[LOAD EFFECT] Данные для fileId ${fileId} уже загружены. Пропускаем повторный запуск.`);
        return;
    }


    const loadOrCreateNote = async () => {
      if (loadingFileIdRef.current !== fileId) {
        console.warn(`[LOAD EFFECT] Aborted loadOrCreateNote: fileId изменился (${loadingFileIdRef.current} != ${fileId})`);
        return;
      }
      try {
        let so = await getSuperObjectByMongoId(noteId);
        let blocksFromDb: ContentBlock[] = [];

        if (!so) {
          console.log(`[LOAD EFFECT] SuperObject для fileId ${fileId} не найден. Создаем новый.`);
          const newSOData: SuperObject = { fileId: fileId, serviceType: 'document', name: `Заметка ${fileId}` };
          const createdSO = await createSuperObject(newSOData);
          if (!createdSO || !createdSO.id) {
            throw new Error('Не удалось создать SuperObject на сервере.');
          }
          so = createdSO;
        } else {
          console.log(`[LOAD EFFECT] SuperObject для fileId ${fileId} найден. Загружаем блоки.`);
          blocksFromDb = await getAllContentBlocksForSuperObject(so);
        }
        
        setCurrentSuperObject(so);
        const initialDataForEditor = convertContentBlocksToEditorJsData(blocksFromDb);
        setEditorInitialDataUsed(initialDataForEditor);
        latestEditorDataRef.current = initialDataForEditor;

      } catch (err: any) {
        console.error(`[LOAD EFFECT] Ошибка при загрузке/создании для fileId ${fileId}:`, err);
        if (loadingFileIdRef.current === fileId) setError(err.message || 'Ошибка при загрузке данных заметки.');
      } finally {
        if (loadingFileIdRef.current === fileId) {
          setIsLoading(false);
        }
      }

    };

    if (isLoading && loadingFileIdRef.current === fileId) {
      loadOrCreateNote();
    }
    
  }, [noteId]);

  const uploaderObject = useMemo(() => {
    return {
      uploadByFile: async (file: File): Promise<ImageUploadResult> => {
        const result = await uploadImageApi(file); 
        if (result.success === 1 && result.file && result.file.url) {
        }
        return result;
      },
    };
  }, []);

  const debouncedSave = useCallback(
    debounce(async () => {
      const soToSave = currentSuperObjectRefForDebounce.current;
      const editorDataToSave = latestEditorDataRef.current;

      if (!soToSave || !soToSave.id || !editorDataToSave ) {
        console.log('[DEBOUNCED SAVE] Нет SO, ID или данных для сохранения.', { soToSave, editorDataToSave });
        return; 
      }

      if (editorDataToSave.blocks.length === 0) {
        console.log('[DEBOUNCED SAVE] Сохранение пустого массива блоков.');
      }

      const payload = convertEditorJsBlocksToPayload(editorDataToSave.blocks);
      
      try {
        const updatedSOFromServer = await syncDocumentBlocksApi(soToSave.id, payload);

        setCurrentSuperObject(prevSO => {
          if (prevSO && prevSO.id === updatedSOFromServer.id) {
            const importantFieldsChanged = 
                prevSO.name !== updatedSOFromServer.name ||
                prevSO.lastChangeDate !== updatedSOFromServer.lastChangeDate ||
                prevSO.firstItem !== updatedSOFromServer.firstItem ||
                prevSO.lastItem !== updatedSOFromServer.lastItem;

            if (importantFieldsChanged) {
              console.log("[DEBOUNCED SAVE] Обновляем стейт currentSuperObject, т.к. важные поля изменились.");
              return { ...prevSO, ...updatedSOFromServer };
            } else {
              console.log("[DEBOUNCED SAVE] Стейт currentSuperObject не требует обновления (важные поля не изменились).");
              return prevSO;
            }
          }
          return prevSO;
        });
      } catch (e: any) {
        console.error('[DEBOUNCED SAVE] Ошибка синхронизации:', e);
        setError(`Ошибка сохранения: ${e.message || 'Неизвестная ошибка'}`);
      }
    }, 2000),
  []);


  const handleEditorChange = useCallback((api: EditorJSAPI, newData: OutputData) => {
    latestEditorDataRef.current = newData;
    debouncedSave();
  }, [debouncedSave]);

  const handleEditorReady = useCallback((editor: EditorJS) => {
    editorJsInstanceRef.current = editor;
  }, []);


  if (isLoading) return <div><p>Загрузка данных заметки...</p></div>;
  if (error) return <div><p>Ошибка: {error}</p></div>;
  if (!currentSuperObject || !currentSuperObject.id) return <div><p>Заметка не найдена или не удалось инициализировать.</p></div>;
  if (editorInitialDataUsed === undefined) return <div><p>Инициализация редактора...</p></div>;

  return (
    <div className="note-editor-widget-container">
      {currentSuperObject.name && <h1>{currentSuperObject.name}</h1>}
      <EditorJsWrapper
        key={currentSuperObject.id.toString()}
        holderId={`editorjs-holder-${currentSuperObject.id}`}
        initialData={editorInitialDataUsed}
        onChange={handleEditorChange}
        onReady={handleEditorReady}
        readOnly={readOnly}
        imageUploader={uploaderObject}
      />
    </div>
  );
};
