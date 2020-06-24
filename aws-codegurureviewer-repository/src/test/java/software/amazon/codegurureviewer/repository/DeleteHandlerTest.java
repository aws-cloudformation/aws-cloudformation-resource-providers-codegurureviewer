package software.amazon.codegurureviewer.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.codegurureviewer.CodeGuruReviewerClient;
import software.amazon.awssdk.services.codegurureviewer.model.DisassociateRepositoryRequest;
import software.amazon.awssdk.services.codegurureviewer.model.DisassociateRepositoryResponse;
import software.amazon.awssdk.services.codegurureviewer.model.RepositoryAssociation;
import software.amazon.awssdk.services.codegurureviewer.model.RepositoryAssociationState;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class DeleteHandlerTest extends AbstractTestBase {

    @Mock
    private AmazonWebServicesClientProxy proxy;

    @Mock
    private ProxyClient<CodeGuruReviewerClient> proxyClient;

    @Mock
    CodeGuruReviewerClient sdkClient;

    @BeforeEach
    public void setup() {
        proxy = new AmazonWebServicesClientProxy(logger, MOCK_CREDENTIALS, () -> Duration.ofSeconds(600).toMillis());
        sdkClient = mock(CodeGuruReviewerClient.class);
        proxyClient = MOCK_PROXY(proxy, sdkClient);
    }

    @Test
    public void handleRequest_SimpleSuccess() {
        final RepositoryAssociation associatedRepositoryAssociation =
                RepositoryAssociation.builder().state(RepositoryAssociationState.ASSOCIATED).build();
        final DisassociateRepositoryResponse disassociateRepositoryResponse = DisassociateRepositoryResponse.builder()
                .repositoryAssociation(associatedRepositoryAssociation)
                .build();
        when(proxyClient.client().disassociateRepository(any(DisassociateRepositoryRequest.class))).thenReturn(disassociateRepositoryResponse);

        final DeleteHandler handler = new DeleteHandler();

        final ResourceModel model = ResourceModel.builder().associationArn("arn:aws:codestar-connections:us-west-2" +
                ":309314943313:connection/adaaeec7-ccd3-46b9-b2b3-976fdd4ca66c").build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();

        final ProgressEvent<ResourceModel, CallbackContext> response = handler.handleRequest(proxy, request,
                new CallbackContext(), proxyClient, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()).isEqualTo(request.getDesiredResourceState());
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();

        verify(proxyClient.client()).disassociateRepository(any(DisassociateRepositoryRequest.class));
    }
}
