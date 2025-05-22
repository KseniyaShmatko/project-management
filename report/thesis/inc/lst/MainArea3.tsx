        if (newProjectName.trim() === selectedProject.name) { return; }
    
        try {
          await onProjectNameUpdate(selectedProject.id, newProjectName.trim());
          setIsEditingProjectName(false);
          setProjectNameError(undefined);
        } catch (error) {
          console.error("Ошибка обновления имени проекта (в MainArea):", error);
          setProjectNameError('Не удалось обновить имя проекта.');
        }
      };
    
    const handleProjectNameCancel = () => {
        setIsEditingProjectName(false);
        setProjectNameError(undefined);
        if (selectedProject) {
            setNewProjectName(selectedProject.name);
        }
    };
    
    if (!selectedProject) {
        return (
        <div className="main-area main-area--empty">
            <Text variant="header-1" className="main-area__placeholder-title">Выберите или создайте проект</Text>
            <Text variant="body-1" color="secondary">
            Чтобы начать работу, выберите проект из списка слева или создайте новый.
            </Text>
        </div>
        );
    }

    return (
        <div className="main-area">