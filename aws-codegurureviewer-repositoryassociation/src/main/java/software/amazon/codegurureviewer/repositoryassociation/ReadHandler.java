package software.amazon.codegurureviewer.repositoryassociation;

import software.amazon.awssdk.services.codegurureviewer.CodeGuruReviewerClient;
import software.amazon.awssdk.services.codegurureviewer.model.AccessDeniedException;
import software.amazon.awssdk.services.codegurureviewer.model.DescribeRepositoryAssociationRequest;
import software.amazon.awssdk.services.codegurureviewer.model.DescribeRepositoryAssociationResponse;
import software.amazon.awssdk.services.codegurureviewer.model.InternalServerException;
import software.amazon.awssdk.services.codegurureviewer.model.NotFoundException;
import software.amazon.awssdk.services.codegurureviewer.model.ThrottlingException;
import software.amazon.awssdk.services.codegurureviewer.model.ValidationException;
import software.amazon.cloudformation.exceptions.CfnAccessDeniedException;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.exceptions.CfnServiceInternalErrorException;
import software.amazon.cloudformation.exceptions.CfnThrottlingException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

public class ReadHandler extends BaseHandlerStd {
    private Logger logger;

    protected ProgressEvent<ResourceModel, CallbackContext> handleRequest(
            final AmazonWebServicesClientProxy proxy,
            final ResourceHandlerRequest<ResourceModel> request,
            final CallbackContext callbackContext,
            final ProxyClient<CodeGuruReviewerClient> proxyClient,
            final Logger logger) {

        this.logger = logger;

        final ResourceModel model = request.getDesiredResourceState();

        return proxy.initiate("AWS-CodeGuruReviewer-RepositoryAssociation::Read", proxyClient, model, callbackContext)
                .translateToServiceRequest(Translator::translateToDescribeRepositoryAssociationRequest)
                .makeServiceCall((awsRequest, sdkProxyClient) -> readResource(awsRequest, sdkProxyClient , model))
                .done(this::constructResourceModelFromResponse);
    }

    /**
     * Implement client invocation of the read request through the proxyClient, which is already initialised with
     * caller credentials, correct region and retry settings
     * @param describeRepositoryAssociationRequest the aws service request to describe a resource
     * @param proxyClient the aws service client to make the call
     * @return describe resource response
     */
    private DescribeRepositoryAssociationResponse readResource(
            final DescribeRepositoryAssociationRequest describeRepositoryAssociationRequest,
            final ProxyClient<CodeGuruReviewerClient> proxyClient,
            final ResourceModel model) {
        DescribeRepositoryAssociationResponse awsResponse = null;
        try {
            awsResponse = proxyClient.injectCredentialsAndInvokeV2(describeRepositoryAssociationRequest,
                    proxyClient.client()::describeRepositoryAssociation);
            logger.log(String.format("DescribeRepositoryAssociation response: %s", awsResponse.toString()));
        } catch (final NotFoundException e) {
            throw new CfnNotFoundException(ResourceModel.TYPE_NAME, model.getName(), e);
        } catch (final InternalServerException e) {
            throw new CfnServiceInternalErrorException(ResourceModel.TYPE_NAME, e);
        } catch (final ValidationException e) {
            throw new CfnInvalidRequestException(ResourceModel.TYPE_NAME, e);
        } catch (final AccessDeniedException e) {
            throw new CfnAccessDeniedException(ResourceModel.TYPE_NAME, e);
        } catch (final ThrottlingException e) {
            throw new CfnThrottlingException(ResourceModel.TYPE_NAME, e);
        }

        logger.log(String.format("%s has successfully been read.", ResourceModel.TYPE_NAME));
        return awsResponse;
    }

    /**
     * Implement client invocation of the read request through the proxyClient, which is already initialised with
     * caller credentials, correct region and retry settings
     * @param awsResponse the aws service describe resource response
     * @return progressEvent indicating success, in progress with delay callback or failed state
     */
    private ProgressEvent<ResourceModel, CallbackContext> constructResourceModelFromResponse(
            final DescribeRepositoryAssociationResponse awsResponse) {
        return ProgressEvent.defaultSuccessHandler(Translator.translateFromReadResponse(awsResponse));
    }
}
