                        {selectedProject.currentUserRole === ProjectRole.OWNER ? "Владелец" : selectedProject.currentUserRole}
                    </Text>
            )}
            <div className="main-area__actions">
                {(canEditProject) && (
                    <Button view="action" size="l" onClick={onCreateNewFile}>
                        <Button.Icon><Plus /></Button.Icon>
                        Создать файл
                    </Button>
                )}
                {isOwner && (
                    <Button view="outlined" size="l" onClick={onOpenAccessModal} title="Управление доступом">
                        <Icon data={Gear} />
                    </Button>
                )}
            </div>
          </header>
        
        {isLoading ? (
            <div className="main-area__loader">
            <Loader size="l" />
            </div>
        ) : (
            <FileGrid files={files} onOpenFile={onOpenFile} onUpdateFileName={onUpdateFileName} currentUserRole={selectedProject.currentUserRole}/>
        )}
        </div>
    );
};

export default MainArea;
