import React, { useEffect, useRef, memo } from 'react';
import EditorJS, { OutputData, API as EditorJSAPI, BlockToolConstructable, ToolSettings } from '@editorjs/editorjs';
import Header from '@editorjs/header';
import List from '@editorjs/list';
import Paragraph from '@editorjs/paragraph';
import Underline from '@editorjs/underline';
import Marker from '@editorjs/marker';
import Strikethrough from 'editorjs-strikethrough';
import { StyleInlineTool } from 'editorjs-style';
import ImageTool from '@editorjs/image'
import Quote from '@editorjs/quote';
import CodeTool from '@editorjs/code';
import Table from '@editorjs/table';  
import Embed from '@editorjs/embed';

import { editorJsRussianLocale } from '../../../shared/i18n/editorjs-ru';

interface EditorTools {
    [toolName: string]: BlockToolConstructable | ToolSettings;
}

interface UploadResponseFormat {
  success: 1 | 0;
  file: {
    url: string;
    name?: string;
    size?: number;
  };
  message?: string;
}

interface EditorJsWrapperProps {
    holderId: string;
    initialData?: OutputData;
    onChange?: (api: EditorJSAPI, newData: OutputData) => void;
    onReady?: (editor: EditorJS) => void;
    readOnly?: boolean;
    imageUploader?: {
      uploadByFile: (file: File) => Promise<UploadResponseFormat>;
      uploadByUrl?: (url: string) => Promise<UploadResponseFormat>;
  };
}

const EditorJsWrapper: React.FC<EditorJsWrapperProps> = ({
  holderId,
  initialData,
  onChange,
  onReady,
  readOnly = false,
  imageUploader,
}) => {
  const editorInstanceRef = useRef<EditorJS | null>(null);
  const holderRef = useRef<HTMLDivElement | null>(null);

  useEffect(() => {
    if (editorInstanceRef.current && editorInstanceRef.current.readOnly && 
        typeof editorInstanceRef.current.readOnly.toggle === 'function') {
      editorInstanceRef.current.readOnly.toggle(readOnly);
    }
  }, [readOnly, holderId]);


  useEffect(() => {
    if (!holderRef.current) {
      return;
    }

    if (editorInstanceRef.current) {
        if (editorInstanceRef.current.readOnly && typeof editorInstanceRef.current.readOnly.toggle === 'function') {
            editorInstanceRef.current.readOnly.toggle(readOnly);
        }
        return;
    }

    const toolsConfig: EditorTools = {
      paragraph: { class: Paragraph as any, inlineToolbar: true },
      header: { class: Header as any, inlineToolbar: true, config: { levels: [1, 2, 3, 4], defaultLevel: 2 } },
      list: { class: List as any, inlineToolbar: true },
      underline: { class: Underline as any, shortcut: 'CMD+U' },
      strikethrough: { class: Strikethrough as any, shortcut: 'CMD+SHIFT+S' },
      marker: { class: Marker as any, shortcut: 'CMD+SHIFT+M' },
      style: {
        class: StyleInlineTool as any,
        config: { style: [ 'color', 'background-color', 'font-size', 'font-family', 'border', 'text-align' ] },
      },
      image: {
        class: ImageTool as any,
        config: {
          uploader: imageUploader ? {
            uploadByFile: imageUploader.uploadByFile,
            uploadByUrl: imageUploader.uploadByUrl,
          } : {
            uploadByFile: (file: File) => {
              return new Promise<UploadResponseFormat>((resolve, reject) => {
                setTimeout(() => {
                  const reader = new FileReader();
                  reader.onloadend = () => {
                    resolve({
                      success: 1,
                      file: { url: reader.result as string },
                    });
                  };
                  reader.onerror = reject;
                  reader.readAsDataURL(file);
                }, 1000);
              });
            },
            uploadByUrl: (url: string) => {
                return Promise.resolve({
                    success: 1,
                    file: { url: url },
                });
            }
          },
          types: 'image/png, image/jpeg, image/gif, image/webp, image/svg+xml', 
        },
      },
      quote: {
        class: Quote,
        inlineToolbar: true,
        shortcut: 'CMD+SHIFT+O',
        config: {
          quotePlaceholder: 'Введите цитату', 
          captionPlaceholder: 'Автор цитаты',
        },
      },
      code: {
        class: CodeTool,
        shortcut: 'CMD+SHIFT+C',
        config: {
          placeholder: 'Введите код',
        },
      },
      table: {
        class: Table as any,
        inlineToolbar: true,
        config: {
          rows: 2,
          cols: 3,
        },
      },
      embed: {
        class: Embed,
        config: {
          services: {
              youtube: true,
              coub: true,
              imgur: true,
              gfycat: true,
              twitch: true,
              vimeo: true,
              github: true,
            }
          }
      },
    };

    const editor = new EditorJS({
      holder: holderRef.current,
      placeholder: 'Начните вводить текст или выберите блок...',
      readOnly: readOnly,
      i18n: editorJsRussianLocale,
      data: initialData || { blocks: [] },
      onReady: () => {
        console.log(`[EditorJsWrapper] Editor.js ГОТОВ для holderId: ${holderId}`);
        editorInstanceRef.current = editor;
        if (onReady) {
          onReady(editor);
        }
      },
      onChange: async (api, event) => {
        if (onChange && editorInstanceRef.current === editor) {
          try {
            const savedData = await editorInstanceRef.current.save();
            onChange(api, savedData);
          } catch (error) {
            console.error(`[EditorJsWrapper MountEffect] Ошибка в editor.save() для ${holderId}:`, error);
          }
        }
      },
      tools: toolsConfig,
    });

    return () => {
      console.log(`[EditorJsWrapper] Попытка уничтожения инстанса для holderId: ${holderId}, созданного в этом эффекте.`);

      const editorToDestroy = editor;
      const currentGlobalInstance = editorInstanceRef.current;

      if (typeof editorToDestroy.destroy === 'function') {
        try {
          editorToDestroy.destroy();
        } catch (e) {
          console.error('[EditorJsWrapper] Ошибка при уничтожении локального инстанса Editor.js:', e);
        }
      }

      if (currentGlobalInstance === editorToDestroy) {
        editorInstanceRef.current = null;
      } else if (currentGlobalInstance) {
        console.warn(`[EditorJsWrapper] Уничтожен инстанс для ${holderId}, но editorInstanceRef.current указывал на ДРУГОЙ (возможно, более новый) инстанс. Это может быть ОК при быстрой смене key.`);
      }
    };
  }, [holderId, onChange, onReady, imageUploader]);

  return <div ref={holderRef} id={holderId} style={{ border: '1px solid #ccc', minHeight: '200px' }} />;
};

export default memo(EditorJsWrapper);
