# AWS::CodeGuruReviewer::RepositoryAssociation

This resource schema represents the RepositoryAssociation resource in the Amazon CodeGuru Reviewer service.

## Syntax

To declare this entity in your AWS CloudFormation template, use the following syntax:

### JSON

<pre>
{
    "Type" : "AWS::CodeGuruReviewer::RepositoryAssociation",
    "Properties" : {
        "<a href="#name" title="Name">Name</a>" : <i>String</i>,
        "<a href="#type" title="Type">Type</a>" : <i>String</i>,
        "<a href="#owner" title="Owner">Owner</a>" : <i>String</i>,
        "<a href="#connectionarn" title="ConnectionArn">ConnectionArn</a>" : <i>String</i>,
    }
}
</pre>

### YAML

<pre>
Type: AWS::CodeGuruReviewer::RepositoryAssociation
Properties:
    <a href="#name" title="Name">Name</a>: <i>String</i>
    <a href="#type" title="Type">Type</a>: <i>String</i>
    <a href="#owner" title="Owner">Owner</a>: <i>String</i>
    <a href="#connectionarn" title="ConnectionArn">ConnectionArn</a>: <i>String</i>
</pre>

## Properties

#### Name

Name of the repository to be associated.

_Required_: Yes

_Type_: String

_Minimum_: <code>1</code>

_Maximum_: <code>100</code>

_Pattern_: <code>^\S[\w.-]*$</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### Type

The type of repository to be associated.

_Required_: Yes

_Type_: String

_Allowed Values_: <code>CodeCommit</code> | <code>Bitbucket</code> | <code>GitHubEnterpriseServer</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### Owner

The owner of the repository. For a Bitbucket repository, this is the username for the account that owns the repository.

_Required_: No

_Type_: String

_Minimum_: <code>1</code>

_Maximum_: <code>100</code>

_Pattern_: <code>^\S(.*\S)?$</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### ConnectionArn

The Amazon Resource Name (ARN) of an AWS CodeStar Connections connection.

_Required_: No

_Type_: String

_Maximum_: <code>256</code>

_Pattern_: <code>arn:aws(-[\w]+)*:.+:.+:[0-9]{12}:.+</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

## Return Values

### Ref

When you pass the logical ID of this resource to the intrinsic `Ref` function, Ref returns the AssociationArn.

### Fn::GetAtt

The `Fn::GetAtt` intrinsic function returns a value for a specified attribute of this type. The following are the available attributes and sample return values.

For more information about using the `Fn::GetAtt` intrinsic function, see [Fn::GetAtt](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/intrinsic-function-reference-getatt.html).

#### AssociationArn

The Amazon Resource Name (ARN) of the repository association.
