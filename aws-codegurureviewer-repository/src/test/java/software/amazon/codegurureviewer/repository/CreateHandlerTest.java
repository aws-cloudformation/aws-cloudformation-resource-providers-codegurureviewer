package software.amazon.codegurureviewer.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.codegurureviewer.CodeGuruReviewerClient;
import software.amazon.awssdk.services.codegurureviewer.model.AccessDeniedException;
import software.amazon.awssdk.services.codegurureviewer.model.AssociateRepositoryRequest;
import software.amazon.awssdk.services.codegurureviewer.model.AssociateRepositoryResponse;
import software.amazon.awssdk.services.codegurureviewer.model.ConflictException;
import software.amazon.awssdk.services.codegurureviewer.model.DescribeRepositoryAssociationRequest;
import software.amazon.awssdk.services.codegurureviewer.model.DescribeRepositoryAssociationResponse;
import software.amazon.awssdk.services.codegurureviewer.model.InternalServerException;
import software.amazon.awssdk.services.codegurureviewer.model.ProviderType;
import software.amazon.awssdk.services.codegurureviewer.model.RepositoryAssociation;
import software.amazon.awssdk.services.codegurureviewer.model.RepositoryAssociationState;
import software.amazon.awssdk.services.codegurureviewer.model.ThrottlingException;
import software.amazon.awssdk.services.codegurureviewer.model.ValidationException;
import software.amazon.cloudformation.exceptions.CfnAccessDeniedException;
import software.amazon.cloudformation.exceptions.CfnAlreadyExistsException;
import software.amazon.cloudformation.exceptions.CfnInternalFailureException;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
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

    @Test
    public void handleRequest_SimpleBitBucketSuccess() {
        final RepositoryAssociation associatedRepositoryAssociation =
                RepositoryAssociation.builder().state(RepositoryAssociationState.ASSOCIATED).build();
        final AssociateRepositoryResponse associateRepositoryResponse = AssociateRepositoryResponse.builder()
                .repositoryAssociation(associatedRepositoryAssociation).build();
        when(proxyClient.client().associateRepository(any(AssociateRepositoryRequest.class))).thenReturn(associateRepositoryResponse);

        final DescribeRepositoryAssociationResponse describeRepositoryAssociationResponse =
                DescribeRepositoryAssociationResponse.builder()
                        .repositoryAssociation(associatedRepositoryAssociation).build();
        when(proxyClient.client().describeRepositoryAssociation(any(DescribeRepositoryAssociationRequest.class))).thenReturn(describeRepositoryAssociationResponse);

        final ResourceModel model = ResourceModel.builder()
                .name("BitBucket")
                .providerType(ProviderType.BITBUCKET.toString())
                .owner("BitBucketOwner")
                .connectionArn("arn:aws:codestar-connections:us-west-2:123456789012:connection/adaaeec7-ccd3-46b9-b2b3-976fdd4ca66c")
                .build();

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

    @Test
    public void handleRequest_exceptions() {
        final ResourceModel model = ResourceModel.builder()
                .providerType(ProviderType.CODE_COMMIT.toString())
                .name("CodeCommit")
                .build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();

        when(proxyClient.client().associateRepository(any(AssociateRepositoryRequest.class))).thenThrow(InternalServerException.class);
        assertThatExceptionOfType(CfnServiceInternalErrorException.class).isThrownBy(() -> handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger));

        when(proxyClient.client().associateRepository(any(AssociateRepositoryRequest.class))).thenThrow(ValidationException.class);
        assertThatExceptionOfType(CfnInvalidRequestException.class).isThrownBy(() -> handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger));

        when(proxyClient.client().associateRepository(any(AssociateRepositoryRequest.class))).thenThrow(AccessDeniedException.class);
        assertThatExceptionOfType(CfnAccessDeniedException.class).isThrownBy(() -> handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger));

        when(proxyClient.client().associateRepository(any(AssociateRepositoryRequest.class))).thenThrow(ConflictException.class);
        assertThatExceptionOfType(CfnAlreadyExistsException.class).isThrownBy(() -> handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger));

        when(proxyClient.client().associateRepository(any(AssociateRepositoryRequest.class))).thenThrow(ThrottlingException.class);
        assertThatExceptionOfType(CfnThrottlingException.class).isThrownBy(() -> handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger));

        when(proxyClient.client().associateRepository(any(AssociateRepositoryRequest.class))).thenThrow(RuntimeException.class);
        assertThatExceptionOfType(CfnInternalFailureException.class).isThrownBy(() -> handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger));
    }
}
