package com.massfords.jaxb;

import com.sun.codemodel.*;
import com.sun.tools.xjc.outline.ClassOutline;

import java.util.Set;

/**
 * Adds the accept method to the bean.
 * 
 * @author utard
 */
public class AddAcceptMethodTransformer {

    public void run(Set<ClassOutline> sorted, JDefinedClass transformer) {
        for (ClassOutline classOutline : sorted) {
            // skip over abstract classes
            if (!classOutline.target.isAbstract()) {
                // add the accept method to the bean
                JDefinedClass beanImpl = classOutline.implClass;
                JMethod acceptMethod = beanImpl.method(JMod.PUBLIC, Object.class, "accept");
                JTypeVar genericType = acceptMethod.generify("T");
                acceptMethod.type(genericType);
                JVar vizParam = acceptMethod.param(transformer.narrow(genericType), "aTransformer");
                JBlock block = acceptMethod.body();
                block._return(vizParam.invoke("transform").arg(JExpr._this()));
            }
        }
    }
}
