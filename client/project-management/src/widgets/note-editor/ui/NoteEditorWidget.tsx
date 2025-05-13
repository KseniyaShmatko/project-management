// src/widgets/note-editor/ui/NoteEditorWidget.tsx
import React, { useState, useEffect, ChangeEvent, useRef } from 'react';
import { SuperObject, ContentBlock, TextBlockData } from '../../../shared/api/models'; // Путь к вашим моделям
import {
  getSuperObjectByFileId,
  createSuperObject,
  updateSuperObject, // Добавим, если будем обновлять метаданные SO
  getAllContentBlocksForSuperObject,
  createContentBlock, // Будем использовать позже для добавления блоков
  updateContentBlock,
} from '../../../shared/api/noteApi';
import { debounce } from 'lodash-es'; 
import { EditableTextBlock } from '../../../entities/content-block/ui/EditableTextBlock'; // Импортируем новый компонент
import './NoteEditorWidget.scss';
import { Descendant, Element as SlateElement, Text as SlateTextType } from 'slate';

// Функция для сериализации Slate AST в строку (если бэк ожидает строку)
const serializeSlateValueToString = (value: Descendant[]): string => {
  return value
    .map(node => {
      if (SlateElement.isElement(node) && node.children) { // Проверяем, что это Element и у него есть children
        // node.children здесь будет типа Descendant[]
        // Если мы ожидаем, что внутри Element всегда CustomText, можно типизировать агрессивнее
        return (node.children as SlateTextType[]).map((child: SlateTextType) => child.text || '').join('');
      } else if (SlateTextType.isText(node)) { // Если это просто текстовая нода верхнего уровня (редко, но возможно)
         return node.text || '';
      }
      return '';
    })
    .join('\n');
};

const serializeSlateAstToJsonString = (value: Descendant[]): string => {
  return JSON.stringify(value);
};

interface NoteEditorWidgetProps {
  noteId?: string; // fileId из URL, который является PK файла в PostgreSQL
}


export const NoteEditorWidget: React.FC<NoteEditorWidgetProps> = ({ noteId }) => {
  const [superObject, setSuperObject] = useState<SuperObject | null>(null);
  const [contentBlocks, setContentBlocks] = useState<ContentBlock[]>([]);
  const [isLoading, setIsLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);
  const isCreatingRef = useRef(false);

  useEffect(() => {
    if (!noteId) { setIsLoading(false); setError('ID заметки не предоставлен.'); return; }
    const fileId = parseInt(noteId, 10);
    if (isNaN(fileId)) { setIsLoading(false); setError('Некорректный ID заметки.'); return; }

    const loadNoteData = async () => {
      console.log(`[EFFECT] loadNoteData called for fileId: ${fileId}`);
      setIsLoading(true); setError(null);
      isCreatingRef.current = false; // Сбрасываем флаг в начале каждой попытки загрузки
  
      try {
        console.log(`[EFFECT] Attempting to GET SuperObject by fileId: ${fileId}`);
        let currentSO = await getSuperObjectByFileId(fileId); // API вызов
        console.log(`[EFFECT] GET SuperObject result:`, currentSO);
  
        let initialBlocks: ContentBlock[] = [];
  
        if (!currentSO) {
          if (isCreatingRef.current) {
            console.log("[EFFECT] Creation already in progress, exiting.");
            return; // Важно не менять isLoading, если мы просто вышли
          }
          isCreatingRef.current = true;
          console.log(`[EFFECT] SuperObject NOT found. Attempting to CREATE new one for fileId: ${fileId}`);
          const newSOData: SuperObject = { fileId: fileId, serviceType: 'document', name: `Заметка ${fileId}` };
          currentSO = await createSuperObject(newSOData); // API вызов
          console.log('[EFFECT] CREATE SuperObject result:', currentSO);
  
          if (currentSO && currentSO.id) { // Добавим проверку currentSO перед currentSO.id
            console.log(`[EFFECT] New SuperObject created. Attempting to CREATE first block.`);
            const firstBlockData: Omit<ContentBlock, 'id'> = {
              objectType: 'text',
              // Используй JSON для Slate AST
              data: serializeSlateAstToJsonString([{ type: 'paragraph', children: [{ text: 'Новый блок...' }] }]),
            };
            const createdBlock = await createContentBlock(firstBlockData as ContentBlock); // API вызов
            console.log('[EFFECT] CREATE first block result:', createdBlock);
            initialBlocks.push(createdBlock);
  
            if (createdBlock.id) {
              console.log(`[EFFECT] First block created. Attempting to UPDATE SuperObject with first/last item.`);
              currentSO = await updateSuperObject(currentSO.id, { // API вызов
                firstItem: createdBlock.id,
                lastItem: createdBlock.id,
                lastChangeDate: new Date().toISOString()
              });
              console.log('[EFFECT] UPDATE SuperObject with first/last item result:', currentSO);
            }
          }
          isCreatingRef.current = false;
        } else { // currentSO существует
          console.log(`[EFFECT] SuperObject FOUND. Attempting to GET all content blocks.`);
          initialBlocks = await getAllContentBlocksForSuperObject(currentSO); // API вызов
          console.log('[EFFECT] GET all content blocks result:', initialBlocks);
  
          if (initialBlocks.length === 0 && currentSO.id && !currentSO.firstItem) {
            // Эта логика может быть избыточной, если создание SO и первого блока атомарно или надежно
            if (isCreatingRef.current) {
               console.log("[EFFECT] Creation (for existing SO) already in progress, exiting.");
               return;
            }
            isCreatingRef.current = true;  
            console.log(`[EFFECT] SO exists, but no blocks/firstItem. Attempting to CREATE first block.`);

           const firstBlockData: Omit<ContentBlock, 'id'> = {
              objectType: 'text',
              data: serializeSlateAstToJsonString([{ type: 'paragraph', children: [{ text: '' }] }]),
           };
           const createdBlock = await createContentBlock(firstBlockData as ContentBlock);
           initialBlocks.push(createdBlock);
           if (createdBlock.id) {
               currentSO = await updateSuperObject(currentSO.id, {
                   firstItem: createdBlock.id,
                   lastItem: createdBlock.id,
                   lastChangeDate: new Date().toISOString()
               });
               console.log('SuperObject обновлен с first/last item (для существующего SO):', currentSO);
           }
           isCreatingRef.current = false;
        }
        }
        console.log('[EFFECT] FINAL setSuperObject:', currentSO);
        console.log('[EFFECT] FINAL setContentBlocks:', initialBlocks);
        setSuperObject(currentSO);
        setContentBlocks(initialBlocks);

      } catch (err: any) {
        console.error('[EFFECT CATCH] Ошибка загрузки/создания:', err);
        setError(err.message || 'Ошибка системы');
        isCreatingRef.current = false;
      } finally {
        console.log('[EFFECT FINALLY] Setting isLoading to false.');
        setIsLoading(false);
      }
    };

    loadNoteData();
    // Функция очистки для useEffect (вызывается StrictMode при "размонтировании")
    return () => {
      console.log(`[EFFECT CLEANUP] for fileId: ${noteId} (StrictMode unmount?)`);
      // Здесь важно не сбрасывать isCreatingRef.current, если мы хотим, чтобы
      // следующий "монтирующий" вызов увидел, что создание уже идет.
      // Но если это реальное размонтирование, то, возможно, стоит отменить асинхронные операции.
    };
  }, [noteId]);

  console.log('contentBlocks', contentBlocks)
  // Функция для обработки изменений в текстовом блоке
  const handleBlockContentChangeOptimistic = (blockId: string, newSlateValue: Descendant[]) => {
    // Эта функция может остаться, если ты хочешь обновлять UI до сохранения
    // на основе Slate AST. Но data блока будет обновляться только после сериализации.
    // Или ее можно убрать, если обновление UI происходит только при изменении строки data.
    console.log('Slate AST изменился для блока (оптимистично):', blockId, newSlateValue);
    // Можно временно сохранить newSlateValue в отдельном состоянии, если нужно
  };

  const handleSaveBlock = async (blockId: string, newStringData: string) => { // Принимаем строку
    const originalBlock = contentBlocks.find(b => b.id === blockId);
    if (originalBlock && originalBlock.data === newStringData) {
      console.log(`Блок ${blockId} (data) не изменился, сохранение пропущено.`);
      return;
    }

    // Оптимистичное обновление data в contentBlocks
    setContentBlocks(prevBlocks =>
        prevBlocks.map(b =>
            b.id === blockId ? { ...b, data: newStringData } : b
        )
    );

    try {
      // Отправляем только ID и новое строковое data,
      // бэкенд должен обновить только это поле у существующего блока,
      // ИЛИ если бэкенд требует весь объект:
      const blockToUpdateOnBackend: Partial<ContentBlock> = { data: newStringData }; 
      // Если бэкенд требует весь объект, то нужно сначала найти текущий блок, 
      // обновить его data и отправить весь блок.
      // const currentBlock = contentBlocks.find(b => b.id === blockId);
      // if (currentBlock) {
      //   const fullBlockToUpdate = { ...currentBlock, data: newStringData };
      //   await updateContentBlock(blockId, fullBlockToUpdate);
      // } else {
      //    throw new Error("Block not found for saving");
      // }
      await updateContentBlock(blockId, blockToUpdateOnBackend); // Предполагаем, что бэк может обновить по Partial

      console.log(`Блок ${blockId} сохранен на бэкенд с data:`, newStringData);
      if (superObject && superObject.id) {
        await updateSuperObject(superObject.id, { lastChangeDate: new Date().toISOString() });
      }
    } catch (err) {
      console.error('Ошибка сохранения блока:', err);
      setError('Ошибка сохранения изменений.');
      // TODO: Откат UI
    }
  };

  const handleCreateNewBlockAfter = async (currentBlockId: string, initialDataForNewBlock?: Descendant[]) => {
    const currentIndex = contentBlocks.findIndex(b => b.id === currentBlockId);
    if (currentIndex === -1 || !superObject || !superObject.id) return;
  
    const newBlockInitialText = initialDataForNewBlock
      ? serializeSlateAstToJsonString(initialDataForNewBlock)
      : serializeSlateAstToJsonString([{ type: 'paragraph', children: [{ text: '' }] }]); // Новый пустой блок
  
    const newBlockData: Omit<ContentBlock, 'id'> = {
      objectType: 'text', // или 'richtext'
      data: newBlockInitialText,
      prevItem: currentBlockId,
      nextItem: contentBlocks[currentIndex].nextItem, // Сохраняем связь со следующим за текущим
    };
  
    try {
      const createdBlock = await createContentBlock(newBlockData as ContentBlock);
      if (!createdBlock || !createdBlock.id) {
        throw new Error("Не удалось создать новый блок на сервере");
      }
  
      // Обновляем состояние contentBlocks на клиенте
      const newBlocks = [...contentBlocks];
      newBlocks.splice(currentIndex + 1, 0, createdBlock); // Вставляем новый блок
  
      // Обновляем nextItem у текущего блока
      newBlocks[currentIndex].nextItem = createdBlock.id;
      if (newBlocks[currentIndex].id) { // Проверка на существование ID
          await updateContentBlock(newBlocks[currentIndex].id!, { nextItem: createdBlock.id });
      }
  
  
      // Обновляем prevItem у блока, который был следующим за текущим (если он был)
      if (createdBlock.nextItem) {
        const nextBlockIndex = newBlocks.findIndex(b => b.id === createdBlock.nextItem);
        if (nextBlockIndex !== -1) {
          newBlocks[nextBlockIndex].prevItem = createdBlock.id;
          if (newBlocks[nextBlockIndex].id) { // Проверка на существование ID
              await updateContentBlock(newBlocks[nextBlockIndex].id!, { prevItem: createdBlock.id });
          }
        }
      }
  
      setContentBlocks(newBlocks);
  
      // Обновляем SuperObject, если новый блок стал последним
      let soUpdates: Partial<SuperObject> = { lastChangeDate: new Date().toISOString() };
      if (!createdBlock.nextItem) { // Если у нового блока нет следующего, он последний
        soUpdates.lastItem = createdBlock.id;
      }
      await updateSuperObject(superObject.id, soUpdates);
  
      // TODO: Установить фокус на новый созданный блок. Это потребует рефов на EditableTextBlock.
      // И, возможно, передачу `autoFocus` пропа в нужный EditableTextBlock.
  
    } catch (err) {
      console.error("Ошибка создания нового блока:", err);
      setError("Не удалось создать новый блок.");
    }
  };
  
  if (isLoading) return <div><p>Загрузка...</p></div>;
  if (error) return <div><p style={{ color: 'red' }}>Ошибка: {error}</p></div>;
  if (!superObject) return <div><p>Заметка не найдена.</p></div>;


  return (
    <div className="note-editor-widget">
      {/* Можно отобразить имя суперобъекта, если оно есть */}
      {superObject.name && <h2>{superObject.name}</h2>}

      <div className="note-paper">
        {contentBlocks.length === 0 && !isLoading && (
          <p>Заметка пуста. Начните вводить текст.</p>
          // TODO: Кнопка "Добавить первый блок"
        )}
        {contentBlocks.map((block) => {
          if (!block.id) return null;

          if (block.objectType === 'text' || block.objectType === 'richtext') {
            return (
              <EditableTextBlock
                key={block.id}
                block={block}
                onContentChange={handleBlockContentChangeOptimistic}
                onSave={handleSaveBlock} 
                onCreateNewBlockAfter={handleCreateNewBlockAfter}// Теперь типы должны совпадать
              />
            );
          }
          // TODO: Рендеринг других типов блоков
          return (
            <div key={block.id} className="content-block unknown-block">
              <p>Нетекстовый блок: {block.objectType}</p>
              <pre>{JSON.stringify(block.data, null, 2)}</pre>
              {block.items && <pre>Items: {JSON.stringify(block.items, null, 2)}</pre>}
            </div>
          );
        })}
        {/* // TODO: Кнопка "Добавить новый блок" в конце */}
      </div>
    </div>
  );
};
