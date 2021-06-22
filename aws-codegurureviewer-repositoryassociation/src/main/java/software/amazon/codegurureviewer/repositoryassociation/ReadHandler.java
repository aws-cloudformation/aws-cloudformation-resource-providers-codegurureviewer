package software.amazon.codegurureviewer.repositoryassociation;

import com.amazonaws.util.StringUtils;
import software.amazon.awssdk.services.codegurureviewer.CodeGuruReviewerClient;
import software.amazon.awssdk.services.codegurureviewer.model.DescribeRepositoryAssociationRequest;
import software.amazon.awssdk.services.codegurureviewer.model.DescribeRepositoryAssociationResponse;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
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
        if (StringUtils.isNullOrEmpty(model.getAssociationArn())) {
            throw new CfnNotFoundException(ResourceModel.TYPE_NAME, null);
        }

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

        DescribeRepositoryAssociationResponse awsResponse = describeRepositoryAssociation(describeRepositoryAssociationRequest, proxyClient, model);
        logger.log(String.format("DescribeRepositoryAssociation response: %s", awsResponse.toString()));
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
