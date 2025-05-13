// src/entities/content-block/ui/EditableTextBlock.tsx
// (Это новый файл, создай его в структуре entities)
import React, { useState, useMemo, useCallback } from 'react';
import {
    createEditor,
    Descendant,
    Editor as SlateLibraryEditor, // Уберем пока прямое использование SlateLibraryEditor в объявлении RichTextEditor
                                  // Оно будет неявно через SlateBaseEditor
    Transforms,
    Node as SlateNode,
    BaseEditor, // Импортируем BaseEditor
    Text as SlateTextNodeBase, // Базовый тип текста
    Range, // <--- ИМПОРТИРУЕМ Range
    Path,  // <--- ИМПОРТИРУЕМ Path
    Element as SlateElement // Базовый тип элемента
} from 'slate';
import {
  Slate,
  Editable,
  withReact,
  ReactEditor, // Импортируем ReactEditor из 'slate-react'
  RenderLeafProps,
  RenderElementProps // Импортируем RenderElementProps из 'slate-react'
} from 'slate-react';
import { debounce } from 'lodash-es';
import { ContentBlock } from '../../../shared/api/models'; // Твой тип
import { updateContentBlock } from '../../../shared/api/noteApi'; // Твоя API-функция

// 1. Определяем наши кастомные типы для элементов и текста
export type CustomText = {
    text: string;
    bold?: boolean;
    italic?: boolean;
    // ...другие маркеры форматирования
};

export type ParagraphElement = {
    type: 'paragraph';
    align?: 'left' | 'center' | 'right' | 'justify';
    children: CustomText[]; // Используем CustomText
}

// В будущем можно добавить:
// export type CustomHeadingElement = { type: 'heading'; level: 1 | 2 | 3; children: CustomTextNode[] };
// export type CustomListItemElement = { type: 'list-item'; children: CustomTextNode[] };
// export type CustomListElement = { type: 'bulleted-list' | 'numbered-list'; children: CustomListItemElement[] };

export type CustomElement = ParagraphElement; // | CustomHeadingElement | ... ;

// --- Расширение типов для Slate ---
// Это стандартный паттерн для расширения типов Slate
// Сначала определяем базовые типы редактора, элемента и текста,
// а потом используем их в `declare module`.

// Наш кастомный тип редактора
export type CustomEditor = BaseEditor & ReactEditor; // Объединяем базовый и React-редактор

// Патчим глобальный тип Slate
declare module 'slate' {
  interface CustomTypes {
    Editor: CustomEditor;     // Используем CustomEditor
    Element: CustomElement;   // Используем наш CustomElement
    Text: CustomText;         // Используем наш CustomText
  }
}

const serializeSlateAstToJsonString = (value: Descendant[]): string => {
    return JSON.stringify(value);
  };
  
  const deserializeJsonStringToSlateAst = (jsonString: string): Descendant[] => {
    try {
      // Начальное значение по умолчанию для пустого редактора
      const defaultValue = [{ type: 'paragraph', children: [{ text: '' }] }] as Descendant[];
      if (!jsonString) {
        return defaultValue;
      }
      const parsed = JSON.parse(jsonString);
      // Простая проверка, что это массив (базовая структура Slate AST)
      return Array.isArray(parsed) ? parsed : defaultValue;
    } catch (e) {
      console.error("Failed to parse Slate AST from JSON:", e);
      // Возвращаем значение по умолчанию при ошибке парсинга
      return [{ type: 'paragraph', children: [{ text: '' }] }];
    }
  };
// --- Компонент ---
interface EditableTextBlockProps {
  block: ContentBlock;
  onContentChange: (blockId: string, newSlateValue: Descendant[]) => void;
  onSave: (blockId: string, newStringData: string) => Promise<void>;
  onCreateNewBlockAfter?: (currentBlockId: string, initialData?: Descendant[]) => void;
  onMergeWithPreviousBlock?: (currentBlockId: string) => void;
}

const isMarkActive = (editor: CustomEditor, format: keyof Omit<CustomText, 'text'>) => {
    const marks = SlateLibraryEditor.marks(editor); // Используем SlateLibraryEditor для статических методов
    return marks ? marks[format] === true : false;
};
  
const toggleMark = (editor: CustomEditor, format: keyof Omit<CustomText, 'text'>) => {
    const isActive = isMarkActive(editor, format);
    if (isActive) {
      SlateLibraryEditor.removeMark(editor, format);
    } else {
      SlateLibraryEditor.addMark(editor, format, true);
    }
};

export const EditableTextBlock: React.FC<EditableTextBlockProps> = ({ block, onContentChange, onSave,  onCreateNewBlockAfter, onMergeWithPreviousBlock }) => {
    // При создании editor, TypeScript теперь должен "знать" о CustomEditor через declare module
    const editor = useMemo(() => withReact(createEditor() as CustomEditor), []);
  
    const initialValue = useMemo((): Descendant[] => {
        if ((block.objectType === 'text' || block.objectType === 'richtext') && typeof block.data === 'string') {
          return deserializeJsonStringToSlateAst(block.data);
        }
        return [{ type: 'paragraph', children: [{ text: '' }] }];
      }, [block.data, block.objectType]);
      
      const debouncedSave = useCallback(
        debounce(async (currentValue: Descendant[]) => {
          if (block.id) {
            const stringData = serializeSlateAstToJsonString(currentValue);
            await onSave(block.id, stringData);
          }
        }, 1500),
        [block.id, onSave]
      );
  
    const handleChange = (currentValue: Descendant[]) => {
      onContentChange(block.id!, currentValue);
      debouncedSave(currentValue);
    };
  
    const renderElement = useCallback((props: RenderElementProps) => {
      const { attributes, children, element } = props;
      switch (element.type) {
        case 'paragraph':
          return <p {...attributes} style={{ textAlign: element.align }}>{children}</p>;
        // case 'heading':
        //   return React.createElement(`h${element.level}`, attributes, children);
        default: // Важно иметь default, чтобы TS был доволен, что все типы CustomElement обработаны
          return <div {...attributes}>{children}</div>;
      }
    }, []);
  
    const renderLeaf = useCallback((props: RenderLeafProps) => {
        let { attributes, children, leaf } = props;
        
        // Оборачиваем только если есть соответствующий маркер
        if (leaf.bold) {
          children = <strong>{children}</strong>;
        }
        if (leaf.italic) {
          children = <em>{children}</em>;
        }
        // Если нет никаких маркеров, не нужно оборачивать в лишний span,
        // если только этот span не несет какие-то общие стили для всех листьев.
        // Но Slate сам добавит необходимые span'ы для data-атрибутов.
        
        // Если children уже React-элемент (из-за вложенных strong/em),
        // то просто передаем атрибуты. Если это строка, то Slate сам обернет,
        // либо мы можем обернуть в <span>.
        // React.isValidElement(children) ? 
        //   React.cloneElement(children, {...attributes, ...children.props}) : // осторожно с атрибутами
        return <span {...attributes}>{children}</span>; // Это самый простой и обычно рабочий вариант
      }, []);
  
    if (block.objectType !== 'text' && block.objectType !== 'richtext') {
      return <div>Блок ({block.objectType}) не редактируется этим компонентом.</div>;
    }
  
    const handleKeyDown = (event: React.KeyboardEvent<HTMLDivElement>) => {
        if (event.key === 'Enter' && !event.shiftKey) { // Shift+Enter = мягкий перенос строки (новый <p> в Slate)
          if (onCreateNewBlockAfter) {
            event.preventDefault(); // Предотвращаем стандартное создание нового параграфа в этом Slate-инстансе
            
            // Опцонально: если хочешь перенести текст после курсора в новый блок:
            // const { selection } = editor;
            // let textAfterCursorAst: Descendant[] | undefined = undefined;
            // if (selection && !Range.isCollapsed(selection)) { /* Обработка выделения - сложнее */ }
            // else if (selection && Range.isCollapsed(selection)) {
            //   const currentPoint = selection.anchor;
            //   const [currentNodeEntry] = SlateLibraryEditor.nodes(editor, { at: currentPoint, match: n => SlateText.isText(n) });
            //   if (currentNodeEntry) {
            //     const textNode = currentNodeEntry[0] as SlateText;
            //     if (currentPoint.offset < textNode.text.length) {
            //       // Есть текст после курсора, нужно его "вырезать" и передать для нового блока
            //       // Это упрощенная логика, нужно аккуратно работать с Transforms.splitNodes и Transforms.removeNodes
            //       // const remainingText = textNode.text.substring(currentPoint.offset);
            //       // textAfterCursorAst = [{ type: 'paragraph', children: [{ text: remainingText }] }];
            //       // Transforms.delete(editor, { at: { path: currentPoint.path, offset: currentPoint.offset }, distance: remainingText.length, unit: 'character'});
            //     }
            //   }
            // }
            // onCreateNewBlockAfter(block.id!, textAfterCursorAst);
      
            // Пока просто создаем новый пустой блок
            onCreateNewBlockAfter(block.id!);
            return;
          }
        } else if (event.key === 'Backspace') {
          const { selection } = editor;
          if (selection && Range.isCollapsed(selection)) {
            const entry = SlateLibraryEditor.nodes(editor, {
              match: n => SlateElement.isElement(n) && SlateLibraryEditor.isBlock(editor, n as SlateElement),
              mode: 'lowest',
            });
            const match = entry.next().value;
    
            if (match) {
              const [node, nodePath] = match;
              // Проверяем, что курсор в самом начале блока ([0,0] - это путь к первому дочернему элементу первого элемента верхнего уровня)
              // Или можно проверить, что selection.anchor.path - это начало nodePath и selection.anchor.offset === 0
              const isAtStartOfBlock = SlateLibraryEditor.isStart(editor, selection.anchor, nodePath) && 
                                     selection.anchor.path.length === nodePath.length + 1 && // Курсор внутри этого элемента
                                     selection.anchor.path[nodePath.length] === 0 && // Первый дочерний элемент (текстовая нода)
                                     selection.anchor.offset === 0;
    
    
              if (isAtStartOfBlock) {
                const content = SlateNode.string(node);
                if (content === '' && onMergeWithPreviousBlock) {
                  event.preventDefault();
                  onMergeWithPreviousBlock(block.id!);
                  return;
                }
              }
            }
          }
        }
      };
    

    return (
        <div className="editable-text-block-wrapper"> {/* Обертка для блока + тулбара */}
          <Slate editor={editor} initialValue={initialValue} onChange={handleChange}>
            {/* Простейший тулбар */}
            <div className="toolbar" style={{ marginBottom: '8px', borderBottom: '1px solid #eee', paddingBottom: '8px' }}>
              <button
                type="button" // Важно для предотвращения submit формы, если редактор внутри формы
                onMouseDown={(event) => { // Используем onMouseDown, чтобы не терять фокус с редактора
                  event.preventDefault();
                  toggleMark(editor, 'bold');
                }}
                style={{ fontWeight: isMarkActive(editor, 'bold') ? 'bold' : 'normal', marginRight: '4px' }}
              >
                B
              </button>
              <button
                type="button"
                onMouseDown={(event) => {
                  event.preventDefault();
                  toggleMark(editor, 'italic');
                }}
                style={{ fontStyle: isMarkActive(editor, 'italic') ? 'italic' : 'normal' }}
              >
                I
              </button>
              {/* TODO: Добавить кнопки для других типов форматирования и блочных элементов */}
            </div>
    
            <Editable
              renderElement={renderElement}
              renderLeaf={renderLeaf}
              placeholder="Введите текст..."
              onKeyDown={handleKeyDown} 
              // autoFocus // Можно включить, если это единственный/первый блок
              className="slate-editable-area" // Добавим класс для стилизации
            />
          </Slate>
        </div>
    );
};
  

// Пример простого тулбара (нужно будет вынести и доработать)
// const Toolbar = ({ editor }) => {
//   return (
//     <div>
//       <button
//         onMouseDown={event => {
//           event.preventDefault();
//           toggleMark(editor, 'bold');
//         }}
//       >
//         Bold
//       </button>
//       <button
//         onMouseDown={event => {
//           event.preventDefault();
//           toggleMark(editor, 'italic');
//         }}
//       >
//         Italic
//       </button>
//     </div>
//   );
// };

// const toggleMark = (editor, format) => {
//   const isActive = isMarkActive(editor, format);
//   if (isActive) {
//     Editor.removeMark(editor, format);
//   } else {
//     Editor.addMark(editor, format, true);
//   }
// };

// const isMarkActive = (editor, format) => {
//   const marks = Editor.marks(editor);
//   return marks ? marks[format] === true : false;
// };
