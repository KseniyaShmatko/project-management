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