package software.amazon.codegurureviewer.repositoryassociation;

import software.amazon.awssdk.services.codegurureviewer.CodeGuruReviewerClient;
import software.amazon.awssdk.services.codegurureviewer.model.ListRepositoryAssociationsResponse;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

public class ListHandler extends BaseHandlerStd{

    @Override
    protected ProgressEvent<ResourceModel, CallbackContext> handleRequest(
            AmazonWebServicesClientProxy proxy,
            ResourceHandlerRequest<ResourceModel> request,
            CallbackContext callbackContext,
            ProxyClient<CodeGuruReviewerClient> proxyClient,
            Logger logger) {

        final ListRepositoryAssociationsResponse response = proxy.injectCredentialsAndInvokeV2(
                Translator.translateToLisRepositoryAssocationResquest(request.getNextToken()),
                proxyClient.client()::listRepositoryAssociations);

        return ProgressEvent.<ResourceModel, CallbackContext>builder()
                .status(OperationStatus.SUCCESS)
                .resourceModels(Translator
                        .translateFromListRepositoryAssocationResponse(response.repositoryAssociationSummaries()))
                .nextToken(response.nextToken())
                .build();
    }
}
