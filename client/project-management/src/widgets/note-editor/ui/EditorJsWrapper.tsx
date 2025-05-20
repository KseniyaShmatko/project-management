// src/widgets/note-editor/ui/EditorJsWrapper.tsx
import React, { useEffect, useRef, memo } from 'react';
import EditorJS, { OutputData, API as EditorJSAPI, BlockToolConstructable, ToolSettings } from '@editorjs/editorjs';
import Header from '@editorjs/header';
import List from '@editorjs/list';
import Paragraph from '@editorjs/paragraph';
// import ColorPlugin from 'editorjs-text-color-plugin'; // Если будете использовать, раскомментируйте
import Underline from '@editorjs/underline';
import Marker from '@editorjs/marker';
import Strikethrough from 'editorjs-strikethrough';
import { StyleInlineTool } from 'editorjs-style';
// import InlineFontSizeTool from 'editorjs-inline-font-size-tool'; // ВРЕМЕННО ОТКЛЮЧЕН
import ImageTool from '@editorjs/image'
import Quote from '@editorjs/quote';   // <--- ИМПОРТ QUOTE
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
    name?: string; // Сделаем опциональными, если бэк не всегда их возвращает
    size?: number;
    // ... любые другие ожидаемые поля
  };
  message?: string; // <--- ДОБАВЛЕНО ОПЦИОНАЛЬНОЕ ПОЛЕ
}

interface EditorJsWrapperProps {
    holderId: string;
    initialData?: OutputData;
    onChange?: (api: EditorJSAPI, newData: OutputData) => void;
    onReady?: (editor: EditorJS) => void;
    readOnly?: boolean;
    imageUploader?: {
      uploadByFile: (file: File) => Promise<UploadResponseFormat>;
      uploadByUrl?: (url: string) => Promise<UploadResponseFormat>; // Опционально
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
  const holderRef = useRef<HTMLDivElement | null>(null); // Ref для DOM-элемента
  const initialDataRef = useRef(initialData);

  useEffect(() => {
    if (editorInstanceRef.current && editorInstanceRef.current.readOnly && 
        typeof editorInstanceRef.current.readOnly.toggle === 'function') {
      // Проверяем, действительно ли нужно менять. Нужно знать предыдущее состояние readOnly.
      // Для простоты пока будем вызывать toggle всегда при изменении пропа.
      // В идеале, это нужно делать, если prop readOnly изменился по сравнению
      // с тем, с которым был инициализирован editor.
      console.log(`[EditorJsWrapper ReadOnlyEffect] Toggling readOnly for ${holderId} to ${readOnly}`);
      editorInstanceRef.current.readOnly.toggle(readOnly);
    }
  }, [readOnly, holderId]); // Зависит от readOnly и holderId


  useEffect(() => {
    if (!holderRef.current) {
      console.log(`[EditorJsWrapper] holderRef for ${holderId} is not ready yet.`);
      return;
    }

    // Если инстанс уже существует И readOnly проп не изменился, ничего не делаем
    if (editorInstanceRef.current) {
        if (editorInstanceRef.current.readOnly && typeof editorInstanceRef.current.readOnly.toggle === 'function') {
            // Сверяем текущее состояние readOnly (если API это позволяет)
            // Это более сложная логика, так как editor.readOnly.isEnabled() может быть не публичным
            // Проще всего - всегда вызывать toggle, если readOnly проп отличается от того, что было при инициализации.
            // Но так как мы здесь, значит instance уже есть.
            // Предположим, что readOnly передается как надо и EditorJS сам им управляет
            // или его нужно обновлять только при изменении пропа.
            // Для простоты, если инстанс есть - мы не пересоздаем его, а только обновляем readOnly.
            
            // Проверяем, нужно ли обновить состояние readOnly
            // Чтобы это работало корректно, нам нужно знать предыдущее значение readOnly, с которым был создан editor.
            // Либо, если editor.readOnly.isReadOnly (гипотетический метод) возвращает текущее состояние.
            // Самый простой вариант - всегда вызывать toggle с новым значением, Editor.js сам разберется.
            console.log(`[EditorJsWrapper] Instance for ${holderId} already exists. Toggling readOnly to ${readOnly}.`);
            editorInstanceRef.current.readOnly.toggle(readOnly);
        }
        return;
    }

    console.log(`[EditorJsWrapper] Инициализация Editor.js на holderId: ${holderId} с initialData:`, initialData);

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
      image: { // <--- ДОБАВЛЕНИЕ ИНСТРУМЕНТА IMAGE
        class: ImageTool as any,
        config: {
          // Если imageUploader передан, используем его
          uploader: imageUploader ? {
            uploadByFile: imageUploader.uploadByFile,
            uploadByUrl: imageUploader.uploadByUrl, // Передаем, если есть
          } : {
            // ЗАГЛУШКА ЗАГРУЗЧИКА, ЕСЛИ НЕ ПЕРЕДАН КАСТОМНЫЙ
            // Вам НУЖНО будет реализовать свой uploader
            uploadByFile: (file: File) => {
              console.warn("[ImageTool PlaceholderUploader] uploadByFile вызван. Вам нужно реализовать свой `imageUploader` prop.");
              return new Promise<UploadResponseFormat>((resolve, reject) => {
                // Имитация задержки и успешного ответа с фейковым URL
                setTimeout(() => {
                  // Для теста можно использовать dataURL, но для продакшена нужен реальный URL с сервера
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
                console.warn("[ImageTool PlaceholderUploader] uploadByUrl вызван. Вам нужно реализовать свой `imageUploader` prop.");
                return Promise.resolve({
                    success: 1,
                    file: { url: url }, // Просто возвращаем тот же URL для заглушки
                });
            }
          },
          // Дополнительные опции для ImageTool (см. документацию @editorjs/image)
          // Например:
          // buttons: ['caption', 'withBorder', 'stretched', 'withBackground'],
          // captionPlaceholder: 'Введите подпись к изображению',
          types: 'image/png, image/jpeg, image/gif, image/webp, image/svg+xml', 
        },
      },
      quote: {
        class: Quote,
        inlineToolbar: true, // Позволяет форматировать текст внутри цитаты
        shortcut: 'CMD+SHIFT+O',
        config: {
          quotePlaceholder: 'Введите цитату', // Перевод нужен будет в i18n
          captionPlaceholder: 'Автор цитаты', // Перевод нужен будет в i18n
        },
      },
      code: {
        class: CodeTool,
        shortcut: 'CMD+SHIFT+C',
        config: {
          placeholder: 'Введите код', // Перевод нужен будет в i18n
        },
      },
      table: {
        class: Table as any,
        inlineToolbar: true, // Позволяет форматировать текст внутри ячеек таблицы
        config: {
          rows: 2, // Начальное количество строк при создании таблицы
          cols: 3, // Начальное количество столбцов
          // withHeadings: true, // Раскомментируйте, если хотите, чтобы по умолчанию первая строка была заголовком
                               // Либо пользователь сможет это включить через настройки таблицы
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
      holder: holderRef.current, // Использование ref на DOM-элемент
      placeholder: 'Начните вводить текст или выберите блок...',
      readOnly: readOnly,
      i18n: editorJsRussianLocale,
      data: initialData || { blocks: [] },
      onReady: () => {
        console.log(`[EditorJsWrapper] Editor.js ГОТОВ для holderId: ${holderId}`);
        editorInstanceRef.current = editor; // Сохраняем инстанс ТОЛЬКО после onReady
        if (onReady) {
          onReady(editor);
        }
      },
      onChange: async (api, event) => {
        if (onChange && editorInstanceRef.current === editor) { // Убедимся, что это наш текущий instance
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

    // editorInstanceRef.current = editor; // Мы эту строку перенесли в onReady

    return () => {
      console.log(`[EditorJsWrapper] Попытка уничтожения инстанса для holderId: ${holderId}, созданного в этом эффекте.`);
      // `editor` здесь - это инстанс, созданный именно в ЭТОМ запуске useEffect.
      // `editorInstanceRef.current` может указывать на ДРУГОЙ (более новый) инстанс, если было быстрое перемонтирование.

      const editorToDestroy = editor; // Замыкаем ссылку на тот editor, который был создан в этом эффекте
      const currentGlobalInstance = editorInstanceRef.current;

      // Удаляем слушатель onChange ПЕРЕД destroy, если это возможно и API предоставляет такой метод
      // editorToDestroy.off('change', ...); // Гипотетический метод, API Editor.js может не иметь public off

      if (typeof editorToDestroy.destroy === 'function') {
        try {
          editorToDestroy.destroy();
          console.log(`[EditorJsWrapper] Инстанс (локальный для эффекта) уничтожен для holderId: ${holderId}`);
        } catch (e) {
          console.error('[EditorJsWrapper] Ошибка при уничтожении локального инстанса Editor.js:', e);
        }
      }

      // Если уничтоженный инстанс был тем, на который указывал глобальный ref, обнуляем глобальный ref.
      // Это важно, чтобы не оставить ссылку на уничтоженный объект.
      if (currentGlobalInstance === editorToDestroy) {
        editorInstanceRef.current = null;
        console.log(`[EditorJsWrapper] editorInstanceRef.current обнулен для holderId: ${holderId} т.к. он был равен уничтоженному инстансу.`);
      } else if (currentGlobalInstance) {
        console.warn(`[EditorJsWrapper] Уничтожен инстанс для ${holderId}, но editorInstanceRef.current указывал на ДРУГОЙ (возможно, более новый) инстанс. Это может быть ОК при быстрой смене key.`);
      }
    };
  }, [holderId, onChange, onReady, imageUploader]); // Зависимости пока такие

  // Важно: присваиваем ref здесь
  return <div ref={holderRef} id={holderId} style={{ border: '1px solid #ccc', minHeight: '200px' }} />;
};

export default memo(EditorJsWrapper);
