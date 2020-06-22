package software.amazon.codegurureviewer.repository;

import software.amazon.awssdk.services.codegurureviewer.CodeGuruReviewerClient;
import software.amazon.awssdk.services.codegurureviewer.model.AccessDeniedException;
import software.amazon.awssdk.services.codegurureviewer.model.AssociateRepositoryRequest;
import software.amazon.awssdk.services.codegurureviewer.model.AssociateRepositoryResponse;
import software.amazon.awssdk.services.codegurureviewer.model.ConflictException;
import software.amazon.awssdk.services.codegurureviewer.model.DescribeRepositoryAssociationResponse;
import software.amazon.awssdk.services.codegurureviewer.model.InternalServerException;
import software.amazon.awssdk.services.codegurureviewer.model.RepositoryAssociationState;
import software.amazon.awssdk.services.codegurureviewer.model.ThrottlingException;
import software.amazon.awssdk.services.codegurureviewer.model.ValidationException;
import software.amazon.cloudformation.exceptions.CfnAccessDeniedException;
import software.amazon.cloudformation.exceptions.CfnAlreadyExistsException;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
import software.amazon.cloudformation.exceptions.CfnServiceInternalErrorException;
import software.amazon.cloudformation.exceptions.CfnThrottlingException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

public class CreateHandler extends BaseHandlerStd {
    private Logger logger;

    @Override
    public ProgressEvent<ResourceModel, CallbackContext> handleRequest(
            final AmazonWebServicesClientProxy proxy,
            final ResourceHandlerRequest<ResourceModel> request,
            final CallbackContext callbackContext,
            final ProxyClient<CodeGuruReviewerClient> proxyClient,
            final Logger logger) {

        this.logger = logger;

        final ResourceModel model = request.getDesiredResourceState();

        return ProgressEvent.progress(model, callbackContext)
                .then(progress ->
                        proxy.initiate("AWS-CodeGuruReviewer-Repository::Create", proxyClient, model, callbackContext)
                                .translateToServiceRequest((Translator::translateToAssociateRepositoryRequest))
                                .makeServiceCall(this::createResource)
                                .stabilize(this::stabilizedOnCreate)
                                .progress())
                .then(progress -> new ReadHandler().handleRequest(proxy, request, callbackContext, proxyClient,
                        logger));
    }

    /**
     * Implement client invocation of the create request through the proxyClient, which is already initialised with
     * caller credentials, correct region and retry settings
     *
     * @param associateRepositoryRequest the aws service request to create a resource
     * @param proxyClient                the aws service client to make the call
     * @return awsResponse create resource response
     */
    private AssociateRepositoryResponse createResource(
            final AssociateRepositoryRequest associateRepositoryRequest,
            final ProxyClient<CodeGuruReviewerClient> proxyClient) {
        AssociateRepositoryResponse awsResponse = null;

        try {
            awsResponse = proxyClient.injectCredentialsAndInvokeV2(associateRepositoryRequest,
                    proxyClient.client()::associateRepository);
        } catch (final InternalServerException e) {
            throw new CfnServiceInternalErrorException(ResourceModel.TYPE_NAME, e);
        } catch (final ValidationException e) {
            throw new CfnInvalidRequestException(ResourceModel.TYPE_NAME, e);
        } catch (final AccessDeniedException e) {
            throw new CfnAccessDeniedException(ResourceModel.TYPE_NAME, e);
        } catch (final ConflictException e) {
            throw new CfnAlreadyExistsException(ResourceModel.TYPE_NAME, ResourceModel.IDENTIFIER_KEY_NAME, e);
        } catch (final ThrottlingException e) {
            throw new CfnThrottlingException(ResourceModel.TYPE_NAME, e);
        }

        logger.log(String.format("%s successfully created.", ResourceModel.TYPE_NAME));
        return awsResponse;
    }

    /**
     * If your resource requires some form of stabilization (e.g. service does not provide strong consistency), you
     * will need to ensure that your code
     * accounts for any potential issues, so that a subsequent read/update requests will not cause any conflicts (e.g
     * . NotFoundException/InvalidRequestException)
     * for more information -> https://docs.aws.amazon
     * .com/cloudformation-cli/latest/userguide/resource-type-test-contract.html
     *
     * @param awsRequest      the aws service request to create a resource
     * @param awsResponse     the aws service response to create a resource
     * @param proxyClient     the aws service client to make the call
     * @param model           resource model
     * @param callbackContext callback context
     * @return boolean state of stabilized or not
     */
    private boolean stabilizedOnCreate(
            final AssociateRepositoryRequest awsRequest,
            final AssociateRepositoryResponse awsResponse,
            final ProxyClient<CodeGuruReviewerClient> proxyClient,
            final ResourceModel model,
            final CallbackContext callbackContext) {

        logger.log(String.format("%s [%s] Checking stablization", ResourceModel.TYPE_NAME,
                model.getPrimaryIdentifier()));

        boolean stabilized = false;
        DescribeRepositoryAssociationResponse describeRepositoryAssociationResponse =
                proxyClient.injectCredentialsAndInvokeV2(Translator.translateToDescribeRepositoryAssociationRequest(awsResponse), proxyClient.client()::describeRepositoryAssociation);
        if (describeRepositoryAssociationResponse.repositoryAssociation().state().equals(RepositoryAssociationState.ASSOCIATED)) {
            stabilized = true;
        }
        logger.log(String.format("%s [%s] creation has stabilized: %s", ResourceModel.TYPE_NAME,
                model.getPrimaryIdentifier(), stabilized));
        return stabilized;
    }

}
