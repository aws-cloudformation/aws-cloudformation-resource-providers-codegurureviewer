package software.amazon.codegurureviewer.repositoryassociation;

import software.amazon.awssdk.awscore.AwsRequest;
import software.amazon.awssdk.awscore.AwsResponse;
import software.amazon.awssdk.services.codegurureviewer.CodeGuruReviewerClient;
import software.amazon.awssdk.services.codegurureviewer.model.AccessDeniedException;
import software.amazon.awssdk.services.codegurureviewer.model.ConflictException;
import software.amazon.awssdk.services.codegurureviewer.model.DisassociateRepositoryRequest;
import software.amazon.awssdk.services.codegurureviewer.model.DisassociateRepositoryResponse;
import software.amazon.awssdk.services.codegurureviewer.model.InternalServerException;
import software.amazon.awssdk.services.codegurureviewer.model.NotFoundException;
import software.amazon.awssdk.services.codegurureviewer.model.ThrottlingException;
import software.amazon.awssdk.services.codegurureviewer.model.ValidationException;
import software.amazon.cloudformation.exceptions.CfnAccessDeniedException;
import software.amazon.cloudformation.exceptions.CfnAlreadyExistsException;
import software.amazon.cloudformation.exceptions.CfnInternalFailureException;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.exceptions.CfnServiceInternalErrorException;
import software.amazon.cloudformation.exceptions.CfnThrottlingException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

public class DeleteHandler extends BaseHandlerStd {
    private Logger logger;

    protected ProgressEvent<ResourceModel, CallbackContext> handleRequest(
            final AmazonWebServicesClientProxy proxy,
            final ResourceHandlerRequest<ResourceModel> request,
            final CallbackContext callbackContext,
            final ProxyClient<CodeGuruReviewerClient> proxyClient,
            final Logger logger) {

        this.logger = logger;

        final ResourceModel model = request.getDesiredResourceState();

        return ProgressEvent.progress(model, callbackContext)
                .then(progress -> checkForPreDeleteResourceExistence(request, progress, proxyClient))
                .then(progress ->
                        proxy.initiate("AWS-CodeGuruReviewer-RepositoryAssociation::Delete", proxyClient, model,
                                callbackContext)
                                .translateToServiceRequest(Translator::translateToDisassociateRepositoryRequest)
                                .backoffDelay(BACKOFF_STRATEGY)
                                .makeServiceCall((awsRequest, sdkProxyClient) -> deleteResource(awsRequest,
                                        sdkProxyClient, model, callbackContext))
                                .stabilize(this::stabilizeOnHandle)
                                .success());
    }

    /**
     * If your service API does not return ResourceNotFoundException on delete requests against some identifier
     * (e.g; resource Name) and instead returns a 200 even though a resource already deleted,
     * you must first check if the resource exists here
     * <p>
     * NOTE: If your service API throws 'ResourceNotFoundException' for delete requests this method is not necessary
     *
     * @param request       incoming resource handler request
     * @param progressEvent event of the previous state indicating success,
     *                      in progress with delay callback or failed state
     * @param proxyClient   the aws service client to make the call
     * @return progressEvent indicating success, in progress with delay callback or failed state
     */
    private ProgressEvent<ResourceModel, CallbackContext> checkForPreDeleteResourceExistence(
            final ResourceHandlerRequest<ResourceModel> request,
            final ProgressEvent<ResourceModel, CallbackContext> progressEvent,
            final ProxyClient<CodeGuruReviewerClient> proxyClient) {
        final ResourceModel model = progressEvent.getResourceModel();
        final CallbackContext callbackContext = progressEvent.getCallbackContext();
        try {
            proxyClient.injectCredentialsAndInvokeV2(Translator.translateToDescribeRepositoryAssociationRequest(model), proxyClient.client()::describeRepositoryAssociation).repositoryAssociation().state();
            return ProgressEvent.progress(model, callbackContext);
        } catch (NotFoundException e) { // ResourceNotFoundException
            if (callbackContext.isDeleteWorkflow()) {
                logger.log(String.format("In a delete workflow. Allow NotFoundException to propagate."));
                return ProgressEvent.progress(model, callbackContext);
            }
            logger.log(String.format("%s does not exist. RequestId: %s. Message: %s",
                    model.getPrimaryIdentifier(),
                    request.getClientRequestToken(),
                    e.getMessage()));
            throw new CfnNotFoundException(e);
        }

    }

    /**
     * Implement client invocation of the delete request through the proxyClient, which is already initialised with
     * caller credentials, correct region and retry settings
     *
     * @param disassociateRepositoryRequest the aws service request to delete a resource
     * @param proxyClient                   the aws service client to make the call
     * @return delete resource response
     */
    private DisassociateRepositoryResponse deleteResource(
            final DisassociateRepositoryRequest disassociateRepositoryRequest,
            final ProxyClient<CodeGuruReviewerClient> proxyClient,
            final ResourceModel model,
            final CallbackContext callbackContext) {
        DisassociateRepositoryResponse awsResponse = null;

        try {
            awsResponse = proxyClient.injectCredentialsAndInvokeV2(disassociateRepositoryRequest,
                    proxyClient.client()::disassociateRepository);
            callbackContext.setDeleteWorkflow(true);
            logger.log(String.format("DisassociateRepository response: %s", awsResponse.toString()));
        } catch (final NotFoundException e) {
            throw new CfnNotFoundException(ResourceModel.TYPE_NAME, model.getName(), e);
        } catch (final InternalServerException e) {
            throw new CfnServiceInternalErrorException(ResourceModel.TYPE_NAME, e);
        } catch (final ValidationException e) {
            throw new CfnInvalidRequestException(ResourceModel.TYPE_NAME, e);
        } catch (final AccessDeniedException e) {
            throw new CfnAccessDeniedException(ResourceModel.TYPE_NAME, e);
        } catch (final ConflictException e) {
            throw new CfnAlreadyExistsException(ResourceModel.TYPE_NAME, model.getName(), e);
        } catch (final ThrottlingException e) {
            throw new CfnThrottlingException(ResourceModel.TYPE_NAME, e);
        } catch (final Exception e) {
            throw new CfnInternalFailureException(e);
        }

        logger.log(String.format("%s successfully deleted.", ResourceModel.TYPE_NAME));
        return awsResponse;
    }

    /**
     * If deletion of your resource requires some form of stabilization (e.g. propagation delay)
     * for more information ->
     * https://docs.aws.amazon.com/cloudformation-cli/latest/userguide/resource-type-test-contract.html
     *
     * @param proxyClient the aws service client to make the call
     * @param model       resource model
     * @return boolean state of stabilized or not
     */
    @Override
    protected boolean stabilizeOnHandle(
            final AwsRequest awsRequest,
            final AwsResponse awsResponse,
            final ProxyClient<CodeGuruReviewerClient> proxyClient,
            final ResourceModel model,
            final CallbackContext callbackContext) {
        boolean stabilized = false;

        try {
            describeRepositoryAssociation(Translator.translateToDescribeRepositoryAssociationRequest(model),
                    proxyClient, model);
        } catch (final CfnNotFoundException e) {
            stabilized = true;
        } catch (final Exception e) {
            logger.log(String.format("%s [%s] encounter exception when verifying stabilization",
                    ResourceModel.TYPE_NAME, model.getPrimaryIdentifier()));
        }

        return stabilized;
    }
}
