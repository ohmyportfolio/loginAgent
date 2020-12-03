package net.mycorp.jimin.base.misc;

import java.io.IOException;
import java.io.Writer;

import org.apache.velocity.context.InternalContextAdapter;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.directive.Directive;
import org.apache.velocity.runtime.directive.DirectiveConstants;
import org.apache.velocity.runtime.parser.node.Node;

import net.mycorp.jimin.base.util.Helper;

public class IfneDirevtive extends Directive {

	@Override
	public String getName() {
		return "ifne";
	}

	@Override
	public int getType() {
		return DirectiveConstants.BLOCK;
	}

	@Override
    public boolean render(InternalContextAdapter context, Writer writer,
            Node node) throws IOException, ResourceNotFoundException,
            ParseErrorException, MethodInvocationException
    {
        Object value = node.jjtGetChild(0).value(context);
        if (Helper.notEmpty(value))
        {
            Node content = node.jjtGetChild(1);
            content.render(context, writer);
        }
        return true;
    }
}
