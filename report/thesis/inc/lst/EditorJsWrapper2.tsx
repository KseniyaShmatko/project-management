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