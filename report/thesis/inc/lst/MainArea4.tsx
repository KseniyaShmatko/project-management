          <header className="main-area__header">
            {isEditingProjectName && canEditProject ? (
              <div className="main-area__project-name-edit">
                <TextInput
                  value={newProjectName}
                  onUpdate={handleProjectNameChange}
                  onKeyPress={(e) => { if (e.key === 'Enter') handleProjectNameSave(); if (e.key === 'Escape') handleProjectNameCancel(); }}
                  size="xl"
                  error={projectNameError}
                  controlRef={projectNameInputRef}
                />
                <Button view="action" onClick={handleProjectNameSave}>
                  Сохранить</Button>
                <Button view="flat" onClick={handleProjectNameCancel}>
                  Отмена</Button>
              </div>
            ) : (
              <div className="main-area__project-name-display" onClick={handleProjectNameEditStart} title="Редактировать имя проекта">
                <Text variant="header-1" className="main-area__project-title">
                  {selectedProject.name}</Text>
                {canEditProject ? (
                  <Button view="flat-secondary" className="main-area__edit-icon-button">
                    <Icon data={Pencil} size={18} />
                  </Button>
                ) : null}
              </div>
            )}
            {selectedProject.currentUserRole && (
                    <Text variant="caption-1" color="secondary" className="main-area__current-role">
                        Ваша роль: 