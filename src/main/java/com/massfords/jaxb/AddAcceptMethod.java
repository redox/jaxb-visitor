package com.massfords.jaxb;

import com.sun.codemodel.*;
import com.sun.tools.xjc.outline.ClassOutline;

import java.util.Set;

/**
 * Adds the accept method to the bean.
 * 
 * @author markford
 */
public class AddAcceptMethod {

    public void run(Set<ClassOutline> sorted, JDefinedClass visitor) {
        for (ClassOutline classOutline : sorted) {
            // skip over abstract classes
            if (!classOutline.target.isAbstract()) {
                // add the accept method to the bean
                JDefinedClass beanImpl = classOutline.implClass;
                JMethod acceptMethod = beanImpl.method(JMod.PUBLIC, void.class, "accept");
                JVar vizParam = acceptMethod.param(visitor, "aVisitor");
                JBlock block = acceptMethod.body();
                JInvocation vizInvocation = block.invoke(vizParam, "visit");
                vizInvocation.arg(JExpr._this());
            }
        }
    }
}
