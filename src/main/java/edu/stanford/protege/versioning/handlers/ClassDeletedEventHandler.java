package edu.stanford.protege.versioning.handlers;


import edu.stanford.protege.versioning.files.FileService;
import edu.stanford.protege.webprotege.ipc.EventHandler;
import org.jetbrains.annotations.NotNull;
import org.semanticweb.owlapi.model.IRI;
import org.springframework.stereotype.Component;

@Component
public class ClassDeletedEventHandler implements EventHandler<ClassDeletedEvent> {

    private final FileService fileService;

    public ClassDeletedEventHandler(FileService fileService) {
        this.fileService = fileService;
    }

    @NotNull
    @Override
    public String getChannelName() {
        return ClassDeletedEvent.CHANNEL;
    }

    @NotNull
    @Override
    public String getHandlerName() {
        return this.getClass().getName();
    }

    @Override
    public Class<ClassDeletedEvent> getEventClass() {
        return ClassDeletedEvent.class;
    }

    @Override
    public void handleEvent(ClassDeletedEvent event) {
        for(IRI deletedIri : event.deletedIris()) {
            fileService.removeFileIfExists(event.projectId(), deletedIri);
        }
    }
}
