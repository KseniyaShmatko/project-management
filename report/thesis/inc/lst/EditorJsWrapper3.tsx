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