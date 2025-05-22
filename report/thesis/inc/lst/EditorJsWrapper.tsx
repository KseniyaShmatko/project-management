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