package software.amazon.codegurureviewer.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.codegurureviewer.CodeGuruReviewerClient;
import software.amazon.awssdk.services.codegurureviewer.model.AccessDeniedException;
import software.amazon.awssdk.services.codegurureviewer.model.DescribeRepositoryAssociationRequest;
import software.amazon.awssdk.services.codegurureviewer.model.DescribeRepositoryAssociationResponse;
import software.amazon.awssdk.services.codegurureviewer.model.InternalServerException;
import software.amazon.awssdk.services.codegurureviewer.model.NotFoundException;
import software.amazon.awssdk.services.codegurureviewer.model.ProviderType;
import software.amazon.awssdk.services.codegurureviewer.model.RepositoryAssociation;
import software.amazon.awssdk.services.codegurureviewer.model.RepositoryAssociationState;
import software.amazon.awssdk.services.codegurureviewer.model.ThrottlingException;
import software.amazon.awssdk.services.codegurureviewer.model.ValidationException;
import software.amazon.cloudformation.exceptions.CfnAccessDeniedException;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.exceptions.CfnServiceInternalErrorException;
import software.amazon.cloudformation.exceptions.CfnThrottlingException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ReadHandlerTest extends AbstractTestBase {

    @Mock
    private AmazonWebServicesClientProxy proxy;

    @Mock
    private ProxyClient<CodeGuruReviewerClient> proxyClient;

    @Mock
    CodeGuruReviewerClient sdkClient;

    private ReadHandler handler;

    @BeforeEach
    public void setup() {
        handler = new ReadHandler();
        proxy = new AmazonWebServicesClientProxy(logger, MOCK_CREDENTIALS, () -> Duration.ofSeconds(600).toMillis());
        sdkClient = mock(CodeGuruReviewerClient.class);
        proxyClient = MOCK_PROXY(proxy, sdkClient);
    }

    @Test
    public void handleRequest_SimpleSuccess() {
        final RepositoryAssociation associatedRepositoryAssociation =
                RepositoryAssociation.builder().associationArn("arn:test:test").state(RepositoryAssociationState.ASSOCIATED).providerType(ProviderType.CODE_COMMIT).build();
        final DescribeRepositoryAssociationResponse describeRepositoryAssociationResponse =
                DescribeRepositoryAssociationResponse.builder()
                        .repositoryAssociation(associatedRepositoryAssociation).build();
        when(proxyClient.client().describeRepositoryAssociation(any(DescribeRepositoryAssociationRequest.class))).thenReturn(describeRepositoryAssociationResponse);

        final ResourceModel model = ResourceModel.builder().associationArn("arn:test:test").build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();

        final ProgressEvent<ResourceModel, CallbackContext> response = handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);

        final ResourceModel responseModel = ResourceModel.builder()
                .associationArn("arn:test:test")
                .state(RepositoryAssociationState.ASSOCIATED.toString())
                .providerType(ProviderType.CODE_COMMIT.toString())
                .build();

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()).isEqualTo(responseModel);
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
    }

    @Test
    public void handleRequest_exceptions() {
        final ResourceModel model = ResourceModel.builder()
                .associationArn("arn:test:test")
                .build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();

        when(proxyClient.client().describeRepositoryAssociation(any(DescribeRepositoryAssociationRequest.class))).thenThrow(NotFoundException.class);
        assertThatExceptionOfType(CfnNotFoundException.class).isThrownBy(() -> handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger));

        when(proxyClient.client().describeRepositoryAssociation(any(DescribeRepositoryAssociationRequest.class))).thenThrow(InternalServerException.class);
        assertThatExceptionOfType(CfnServiceInternalErrorException.class).isThrownBy(() -> handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger));

        when(proxyClient.client().describeRepositoryAssociation(any(DescribeRepositoryAssociationRequest.class))).thenThrow(ValidationException.class);
        assertThatExceptionOfType(CfnInvalidRequestException.class).isThrownBy(() -> handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger));

        when(proxyClient.client().describeRepositoryAssociation(any(DescribeRepositoryAssociationRequest.class))).thenThrow(AccessDeniedException.class);
        assertThatExceptionOfType(CfnAccessDeniedException.class).isThrownBy(() -> handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger));

        when(proxyClient.client().describeRepositoryAssociation(any(DescribeRepositoryAssociationRequest.class))).thenThrow(ThrottlingException.class);
        assertThatExceptionOfType(CfnThrottlingException.class).isThrownBy(() -> handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger));
    }
}
