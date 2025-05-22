    const projectNameInputRef = useRef<HTMLInputElement>(null);

    const canEditProject = selectedProject?.currentUserRole === ProjectRole.OWNER || selectedProject?.currentUserRole === ProjectRole.EDITOR;
    const isOwner = selectedProject?.currentUserRole === ProjectRole.OWNER;

    useEffect(() => {
        if (selectedProject) {
            setNewProjectName(selectedProject.name);
            setIsEditingProjectName(false);
        }
    }, [selectedProject]);
    
    useEffect(() => {
        if (isEditingProjectName && projectNameInputRef.current) {
            projectNameInputRef.current.focus();
            projectNameInputRef.current.select();
        }
    }, [isEditingProjectName]);
    
    const handleProjectNameEditStart = () => {
        if (selectedProject) {
            setNewProjectName(selectedProject.name);
            setIsEditingProjectName(true);
        }
    };
    
    const handleProjectNameChange = (value: string) => {
        setNewProjectName(value);
        if (projectNameError && value.trim()) {
            setProjectNameError(undefined);
        }
    };
    
    const handleProjectNameSave = async () => {
        if (!selectedProject || !newProjectName.trim()) { return; }