package edu.stanford.protege.versioning.config;


import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.module.SimpleModule;
import edu.stanford.protege.versioning.entity.GetProjectEntityInfoRequest;
import edu.stanford.protege.versioning.entity.GetProjectEntityInfoResponse;
import edu.stanford.protege.versioning.owl.commands.GetAllOwlClassesRequest;
import edu.stanford.protege.versioning.owl.commands.GetAllOwlClassesResponse;
import edu.stanford.protege.versioning.services.storage.*;
import edu.stanford.protege.webprotege.common.*;
import edu.stanford.protege.webprotege.ipc.CommandExecutor;
import edu.stanford.protege.webprotege.ipc.impl.CommandExecutorImpl;
import edu.stanford.protege.webprotege.jackson.IriDeserializer;
import edu.stanford.protege.webprotege.jackson.*;
import edu.stanford.protege.webprotege.project.*;
import edu.stanford.protege.webprotege.revision.ChangeHistoryFileFactory;
import io.minio.MinioClient;
import org.semanticweb.owlapi.model.IRI;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.util.UrlPathHelper;
import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;

import java.io.File;

@Configuration
public class ApplicationBeans implements WebMvcConfigurer {

    @Override
    public void configurePathMatch(PathMatchConfigurer configurer) {
        UrlPathHelper urlPathHelper = new UrlPathHelper();
        urlPathHelper.setUrlDecode(false);
        configurer.setUrlPathHelper(urlPathHelper);
    }

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new WebProtegeJacksonApplication().objectMapper(new OWLDataFactoryImpl());
        SimpleModule module = new SimpleModule("linearizationModule");
        module.addDeserializer(IRI.class, new IriDeserializer());
        module.addSerializer(IRI.class, new IriSerializer());
        module.addDeserializer(UserId.class, new UserIdDeserializer());
        module.addSerializer(UserId.class, new UserIdSerializer());
        objectMapper.registerModule(module);
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        return objectMapper;
    }

    @Bean
    CommandExecutor<CreateNewProjectAction, CreateNewProjectResult> createNewProjectExecutor() {
        return new CommandExecutorImpl<>(CreateNewProjectResult.class);
    }

    @Bean
    MinioClient minioClient(MinioProperties properties) {
        return MinioClient.builder()
                .credentials(properties.getAccessKey(), properties.getSecretKey())
                .endpoint(properties.getEndPoint())
                .build();
    }


    @Bean
    DataDirectoryProvider getDataDirectoryProvider() {
        return new DataDirectoryProvider();
    }

    @Bean
    @DataDirectory
    public File provideDataDirectory(DataDirectoryProvider provider) {
        return provider.get();
    }

    @Bean
    edu.stanford.protege.webprotege.revision.ProjectDirectoryFactory projectDirectoryFactoryRev(@DataDirectory File dataDirectory) {
        return new edu.stanford.protege.webprotege.revision.ProjectDirectoryFactory(dataDirectory);
    }

    @Bean
    ChangeHistoryFileFactory getChangeHistoryFileFactory(edu.stanford.protege.webprotege.revision.ProjectDirectoryFactory projectDirectoryFactory) {
        return new ChangeHistoryFileFactory(projectDirectoryFactory);
    }

    @Bean
    CommandExecutor<GetAllOwlClassesRequest, GetAllOwlClassesResponse> executorForPostCoordination() {
        return new CommandExecutorImpl<>(GetAllOwlClassesResponse.class);
    }


    @Bean
    CommandExecutor<GetProjectEntityInfoRequest, GetProjectEntityInfoResponse> getEntityRequest(){
        return new CommandExecutorImpl<>(GetProjectEntityInfoResponse.class);
    }
}
