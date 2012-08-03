package com.massfords.jaxb;

import com.sun.codemodel.*;
import com.sun.tools.xjc.model.CPropertyInfo;
import com.sun.tools.xjc.outline.ClassOutline;
import com.sun.tools.xjc.outline.FieldOutline;
import com.sun.tools.xjc.outline.Outline;

import javax.xml.bind.JAXBElement;
import java.util.*;

/**
 * Creates an implementation of the traverser that traverses the beans in depth first order 
 * according to the order returned from the field iterator within the code model.
 * 
 * The default traverser will traverse each of the child beans that are not null. 
 * 
 * @author markford
 */
public class CreateDepthFirstTraverserClass extends CodeCreator {
    
    private JDefinedClass visitor;
    private JDefinedClass traverser;
    private JDefinedClass visitable;

    public CreateDepthFirstTraverserClass(JDefinedClass visitor, JDefinedClass traverser, JDefinedClass visitable,
                                          Outline outline, JPackage jPackageackage) {
        super(outline, jPackageackage);
        this.visitor = visitor;
        this.traverser = traverser;
        this.visitable = visitable;
    }

    @Override
    protected void run(Set<ClassOutline> classes) {
    	
    	// create the class
        JDefinedClass defaultTraverser = getOutline().getClassFactory().createClass(getPackage(),
                "DepthFirstTraverserImpl", null);
        defaultTraverser._implements(traverser);

        setOutput( defaultTraverser );
        
        // build set of classes that we can traverse
        Set<JType> traversalTypes = new HashSet<JType>();
        for (ClassOutline classOutline : getOutline().getClasses()) {
            traversalTypes.add(classOutline.implClass);
        }

        for(ClassOutline classOutline : classes) {
            if (classOutline.target.isAbstract()) {
                continue;
            }
            // add the bean to the traverserImpl
            JMethod traverseMethodImpl = defaultTraverser.method(JMod.PUBLIC, void.class, "traverse");
            JVar beanParam = traverseMethodImpl.param(classOutline.implClass, "aBean");
            JVar vizParam = traverseMethodImpl.param(visitor, "aVisitor");
            JBlock traverseBlock = traverseMethodImpl.body();
            // for each field, if it's a bean, then visit it
            List<FieldOutline> fields = findAllDeclaredAndInheritedFields(classOutline);
            for(FieldOutline fieldOutline : fields) {
                JType rawType = fieldOutline.getRawType();
                JMethod getter = getter(fieldOutline);
                boolean isJAXBElement = isJAXBElement(getter.type());
                CPropertyInfo propertyInfo = fieldOutline.getPropertyInfo();
                boolean isCollection = propertyInfo.isCollection();
                if (isCollection) {
                    JClass collClazz = (JClass) rawType;
                    JClass collType = collClazz.getTypeParameters().get(0);
                    if (traversalTypes.contains(collType)) {
                        JForEach forEach = traverseBlock.forEach(((JClass)rawType).getTypeParameters().get(0), "bean", JExpr.invoke(beanParam, getter));
                        forEach.body().invoke(JExpr.ref("bean"), "accept").arg(vizParam);
                    } else if (collType.name().startsWith("JAXBElement")) {
                        // parameterized type shouldn't be primitive because it can't be visited.
//                        if (collType.isParameterized()) {
//                            JClass paramType = collType.getTypeParameters().get(0);
//                            if (paramType.name().startsWith("?")) {
//                                // when we have a wildcard we should use the bounding class.
//                                paramType = paramType._extends();
//                            }
//                            if (visitable.isAssignableFrom(paramType)) {
//                                JForEach forEach = traverseBlock.forEach(collType, "jaxbElement", JExpr.invoke(beanParam, getter));
//                                forEach.body()._if(JExpr.ref("jaxbElement").ne(JExpr._null()))._then().invoke(JExpr.ref("jaxbElement").invoke("getValue"), "accept").arg(vizParam);
//                            }
//                        } else {
//                            // if the JAXBElement isn't parameterized then treat it as java.lang.Object and test for Visitable interface
                            JForEach forEach = traverseBlock.forEach(collType, "obj", JExpr.invoke(beanParam, getter));
                            forEach.body()._if(JExpr.ref("obj")._instanceof(visitable))._then().invoke(JExpr.cast(visitable, JExpr.ref("obj")), "accept").arg(vizParam);
//                        }
                         
                    } else if (collType.fullName().equals("java.lang.Object")) { 
                        JForEach forEach = traverseBlock.forEach(collType, "obj", JExpr.invoke(beanParam, getter));
                        forEach.body()._if(JExpr.ref("obj")._instanceof(visitable))._then().invoke(JExpr.cast(visitable, JExpr.ref("obj")), "accept").arg(vizParam);
                    }
                } else if (isJAXBElement) { //(isTraversable(rawType, traversalTypes)) {
                    traverseBlock._if(
                            JExpr.invoke(beanParam, getter).ne(JExpr._null())
                            .cand(
                            JExpr.invoke(beanParam, getter).invoke("getValue")._instanceof(visitable))  )._then()
                            .invoke(JExpr.cast(visitable, JExpr.invoke(beanParam, getter).invoke("getValue")), "accept").arg(vizParam);
//                    traverseBlock._if(JExpr.invoke(beanParam, getter).ne(JExpr._null()))._then()
//                            ._if(JExpr.invoke(beanParam, getter).invoke("getValue")._instanceof(visitable))._then()
//                            .invoke(JExpr.invoke(beanParam, getter).invoke("getValue"), "accept").arg(vizParam);
                } else if (isTraversable(rawType, traversalTypes)) {
                    traverseBlock._if(JExpr.invoke(beanParam, getter).ne(JExpr._null()))._then().invoke(JExpr.invoke(beanParam, getter), "accept").arg(vizParam);
                }
            }
        }
    }

    protected List<FieldOutline> findAllDeclaredAndInheritedFields(ClassOutline classOutline) {
        List<FieldOutline> fields = new LinkedList<FieldOutline>();
        ClassOutline currentClassOutline = classOutline;
        while(currentClassOutline != null) {
            fields.addAll(Arrays.asList(currentClassOutline.getDeclaredFields()));
            currentClassOutline = currentClassOutline.getSuperClass();
        }
        return fields;
    }

    /**
     * Returns true if the type is a JAXBElement. In the case of JAXBElements, we want to traverse its
     * underlying value as opposed to the JAXBElement.
     * @param type
     */
    private boolean isJAXBElement(JType type) {
    	if (type.fullName().startsWith(JAXBElement.class.getName())) {
    		return true;
    	}
		return false;
	}

	/**
	 * Returns true if the type is something that we should traverse. We want to traverse all of the 
	 * beans that were generated. We also include JAXBElement and collections of beans.
	 * 
	 * @param rawType
	 * @param traversalTypes
	 */
	private boolean isTraversable(JType rawType, Set<JType> traversalTypes) {
        if (rawType.isPrimitive()) {
            return false;
        }
        JClass clazz = (JClass) rawType;
        if (clazz.isParameterized()) {
            clazz = clazz.getTypeParameters().get(0);
            if (clazz.name().startsWith("?")) {
                // when we have a wildcard we should use the bounding class.
                clazz = clazz._extends();
            }
        }
        return visitable.isAssignableFrom(clazz);
//        if (traversalTypes.contains(rawType))
//            return true;
//        if (rawType.isPrimitive())
//            return false;
//        JClass clazz = (JClass) rawType;
//        for(JClass arg : clazz.getTypeParameters()) {
//            if (traversalTypes.contains(arg))
//                return true;
//        }
//
//        return false;
    }

    private static final JType[] NONE = new JType[0];
    /**
     * Borrowed this code from jaxb-commons project
     * 
     * @param fieldOutline
     */
    private static JMethod getter(FieldOutline fieldOutline) {
        final JDefinedClass theClass = fieldOutline.parent().implClass;
        final String publicName = fieldOutline.getPropertyInfo().getName(true);
        final JMethod getgetter = theClass.getMethod("get" + publicName, NONE);
        if (getgetter != null) {
            return getgetter;
        } else {
            final JMethod isgetter = theClass
                    .getMethod("is" + publicName, NONE);
            if (isgetter != null) {
                return isgetter;
            } else {
                return null;
            }
        }
    }
}
