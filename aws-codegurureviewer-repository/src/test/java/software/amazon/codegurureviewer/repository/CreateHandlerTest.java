package software.amazon.codegurureviewer.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.codegurureviewer.CodeGuruReviewerClient;
import software.amazon.awssdk.services.codegurureviewer.model.AssociateRepositoryRequest;
import software.amazon.awssdk.services.codegurureviewer.model.AssociateRepositoryResponse;
import software.amazon.awssdk.services.codegurureviewer.model.DescribeRepositoryAssociationRequest;
import software.amazon.awssdk.services.codegurureviewer.model.DescribeRepositoryAssociationResponse;
import software.amazon.awssdk.services.codegurureviewer.model.ProviderType;
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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CreateHandlerTest extends AbstractTestBase {

    @Mock
    private AmazonWebServicesClientProxy proxy;

    @Mock
    private ProxyClient<CodeGuruReviewerClient> proxyClient;

    @Mock
    CodeGuruReviewerClient sdkClient;

    private CreateHandler handler;

    @BeforeEach
    public void setup() {
        handler = new CreateHandler();
        proxy = new AmazonWebServicesClientProxy(logger, MOCK_CREDENTIALS, () -> Duration.ofSeconds(600).toMillis());
        sdkClient = mock(CodeGuruReviewerClient.class);
        proxyClient = MOCK_PROXY(proxy, sdkClient);
    }

    @Test
    public void handleRequest_SimpleSuccess() {
        final RepositoryAssociation associatedRepositoryAssociation =
                RepositoryAssociation.builder().state(RepositoryAssociationState.ASSOCIATED).build();
        final AssociateRepositoryResponse associateRepositoryResponse = AssociateRepositoryResponse.builder()
                .repositoryAssociation(associatedRepositoryAssociation).build();
        when(proxyClient.client().associateRepository(any(AssociateRepositoryRequest.class))).thenReturn(associateRepositoryResponse);

        final DescribeRepositoryAssociationResponse describeRepositoryAssociationResponse =
                DescribeRepositoryAssociationResponse.builder()
                .repositoryAssociation(associatedRepositoryAssociation).build();
        when(proxyClient.client().describeRepositoryAssociation(any(DescribeRepositoryAssociationRequest.class))).thenReturn(describeRepositoryAssociationResponse);

        final ResourceModel model = ResourceModel.builder().providerType(ProviderType.CODE_COMMIT.toString()).name(
                "CodeCommit").build();

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
    }
}
