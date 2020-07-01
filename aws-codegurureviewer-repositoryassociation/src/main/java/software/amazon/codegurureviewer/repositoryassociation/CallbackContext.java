package software.amazon.codegurureviewer.repositoryassociation;

import software.amazon.cloudformation.proxy.StdCallbackContext;

@lombok.Getter
@lombok.Setter
@lombok.ToString
@lombok.EqualsAndHashCode(callSuper = true)
public class CallbackContext extends StdCallbackContext {
    private boolean isCreateWorkflow = false;
    private boolean isDeleteWorkflow = false;
}
