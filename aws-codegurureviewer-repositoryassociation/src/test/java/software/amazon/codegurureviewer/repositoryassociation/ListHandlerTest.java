package software.amazon.codegurureviewer.repositoryassociation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.codegurureviewer.CodeGuruReviewerClient;
import software.amazon.awssdk.services.codegurureviewer.model.ListRepositoryAssociationsResponse;
import software.amazon.awssdk.services.codegurureviewer.model.ProviderType;
import software.amazon.awssdk.services.codegurureviewer.model.RepositoryAssociationState;
import software.amazon.awssdk.services.codegurureviewer.model.RepositoryAssociationSummary;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
public class ListHandlerTest extends AbstractTestBase {

    @Mock
    private AmazonWebServicesClientProxy proxy;
    @Mock
    private Logger logger;
    @Mock
    private ProxyClient<CodeGuruReviewerClient> proxyClient;
    @Mock
    CodeGuruReviewerClient sdkClient;

    private ListHandler handler;

    @BeforeEach
    public void setup() {
        handler = new ListHandler();
        proxy = mock(AmazonWebServicesClientProxy.class);
        proxyClient = MOCK_PROXY(proxy, sdkClient);
        logger = mock(Logger.class);
    }

    @Test
    public void handleRequest_Success() {
        final RepositoryAssociationSummary summary1 =
                RepositoryAssociationSummary.builder()
                .associationArn("arn:test:test1")
                .state(RepositoryAssociationState.ASSOCIATED)
                .providerType(ProviderType.CODE_COMMIT)
                .build();
        final RepositoryAssociationSummary summary2 = RepositoryAssociationSummary.builder()
                .associationArn("arn:test:test2")
                .state(RepositoryAssociationState.FAILED)
                .providerType(ProviderType.GIT_HUB)
                .build();
        final ListRepositoryAssociationsResponse listRepositoryAssociationsResponse =
                ListRepositoryAssociationsResponse.builder()
                .repositoryAssociationSummaries(Arrays.asList(summary1, summary2))
                .nextToken("nextToken")
                .build();

        final ResourceModel model1 = ResourceModel.builder()
                .associationArn("arn:test:test1")
                .type(ProviderType.CODE_COMMIT.toString())
                .build();
        final ResourceModel model2 = ResourceModel.builder()
                .associationArn("arn:test:test2")
                .type(ProviderType.GIT_HUB.toString())
                .build();
        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .nextToken("token")
                .build();

        doReturn(listRepositoryAssociationsResponse)
                .when(proxy)
                .injectCredentialsAndInvokeV2(ArgumentMatchers.any(), ArgumentMatchers.any());

        final ProgressEvent<ResourceModel, CallbackContext> response = handler.handleRequest(proxy, request, null, proxyClient, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackContext()).isNull();
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()).isNull();
        assertThat(response.getResourceModels()).containsAll(Arrays.asList(model1, model2));
        assertThat(response.getNextToken()).isEqualTo("nextToken");
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
    }

    @Test
    public void handleRequest_Success_EmptyResponse() {
        final ListRepositoryAssociationsResponse listRepositoryAssociationsResponse =
                ListRepositoryAssociationsResponse.builder()
                        .repositoryAssociationSummaries(Arrays.asList())
                        .nextToken(null)
                        .build();
        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .nextToken("token")
                .build();

        doReturn(listRepositoryAssociationsResponse)
                .when(proxy)
                .injectCredentialsAndInvokeV2(ArgumentMatchers.any(), ArgumentMatchers.any());

        final ProgressEvent<ResourceModel, CallbackContext> response = handler.handleRequest(proxy, request, null, proxyClient, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackContext()).isNull();
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()).isNull();
        assertThat(response.getResourceModels()).containsAll(Arrays.asList());
        assertThat(response.getNextToken()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
    }
}
