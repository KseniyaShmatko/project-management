              github: true,
            } } }, };
    const editor = new EditorJS({
      holder: holderRef.current,
      placeholder: 'Начните вводить текст или выберите блок...',
      readOnly: readOnly,
      i18n: editorJsRussianLocale,
      data: initialData || { blocks: [] },
      onReady: () => {
        editorInstanceRef.current = editor;
        if (onReady) {
          onReady(editor);
        }
      },
      onChange: async (api, event) => {
        if (onChange && editorInstanceRef.current === editor) {
          const savedData = await editorInstanceRef.current.save();
          onChange(api, savedData);
        }
      },
      tools: toolsConfig,
    });
    return () => {
      const editorToDestroy = editor;
      const currentGlobalInstance = editorInstanceRef.current;
      if (typeof editorToDestroy.destroy === 'function') {
        editorToDestroy.destroy();
      }
      if (currentGlobalInstance === editorToDestroy) {
        editorInstanceRef.current = null;
      }
    };
  }, [holderId, onChange, onReady, imageUploader]);
  return <div ref={holderRef} id={holderId} style={{ border: '1px solid #ccc', minHeight: '200px' }} />;
};
export default memo(EditorJsWrapper);
