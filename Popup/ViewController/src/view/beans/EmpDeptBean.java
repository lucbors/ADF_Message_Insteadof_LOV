package view.beans;

import java.math.BigDecimal;

import java.util.Iterator;

import javax.el.ELContext;

import javax.el.ExpressionFactory;

import javax.el.ValueExpression;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;

import oracle.adf.view.rich.component.rich.RichPopup;
import oracle.adf.view.rich.event.LaunchPopupEvent;

import oracle.adfinternal.view.faces.model.binding.FacesCtrlLOVBinding;

import oracle.binding.BindingContainer;

import org.apache.myfaces.trinidad.render.ExtendedRenderKitService;
import org.apache.myfaces.trinidad.util.Service;

public class EmpDeptBean {
    public EmpDeptBean() {
    }

    public void openJobsLov(LaunchPopupEvent launchPopupEvent) {
        // Add event code here...
        BindingContainer bindings = getBindingContainer();
        FacesCtrlLOVBinding lov = (FacesCtrlLOVBinding) bindings.get("JobId");

        BigDecimal bCurrentSalary = (BigDecimal) evaluateEL("#{bindings.Salary.inputValue}");

        lov.getListIterBinding()
           .getViewObject()
           .setNamedWhereClauseParam("BCurrentSalary", bCurrentSalary);

        Number rowsFetched = lov.getListIterBinding()
                                .getViewObject()
                                .getEstimatedRowCount();

        if (rowsFetched.intValue() == 0) {
            try {
                Exception ex = new Exception("EMP-00001 : No jobs within your salary range");
                throw (ex);
            } catch (Exception e) {
                FacesContext fc = FacesContext.getCurrentInstance();
                fc.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Info", e.getMessage()));
                RichPopup lovPopup = (RichPopup) findComponentInRoot("jobIdId_afrLovDialogId");
                hidePopup(lovPopup);
            }
        }   
    }

    //  launchPopupEvent.setLaunchPopup(false);



    public void hidePopup(RichPopup popup) {
        FacesContext context = FacesContext.getCurrentInstance();
        String popupClientId = popup.getClientId(context);
        StringBuilder script = new StringBuilder();
        script.append("var popup = AdfPage.PAGE.findComponent('")
              .append(popupClientId)
              .append("'); ")
              .append("popup.hide();");
        ExtendedRenderKitService erks = Service.getService(context.getRenderKit(), ExtendedRenderKitService.class);
        erks.addScript(context, script.toString());
    }

    //The two below are typically found in ADFUtils


    public static Object evaluateEL(String el) {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        ELContext elContext = facesContext.getELContext();
        ExpressionFactory expressionFactory = facesContext.getApplication().getExpressionFactory();
        ValueExpression exp = expressionFactory.createValueExpression(elContext, el, Object.class);

        return exp.getValue(elContext);
    }

    public static BindingContainer getBindingContainer() {
        FacesContext fc = FacesContext.getCurrentInstance();
        return fc.getApplication().evaluateExpressionGet(fc, "#{bindings}", BindingContainer.class);
    }
    //The two below are typically found in JSFUtils


    /**
     * Locate an UIComponent in view root with its component id. Use a recursive way to achieve this.
     * @param id UIComponent id
     * @return UIComponent object
     */
    public static UIComponent findComponentInRoot(String id) {
        UIComponent component = null;
        FacesContext facesContext = FacesContext.getCurrentInstance();
        if (facesContext != null) {
            UIComponent root = facesContext.getViewRoot();
            component = findComponent(root, id);
        }
        return component;
    }

    /**
     * Locate an UIComponent from its root component.
     * Taken from http://www.jroller.com/page/mert?entry=how_to_find_a_uicomponent
     * @param base root Component (parent)
     * @param id UIComponent id
     * @return UIComponent object
     */
    public static UIComponent findComponent(UIComponent base, String id) {
        if (id.equals(base.getId()))
            return base;

        UIComponent children = null;
        UIComponent result = null;
        Iterator childrens = base.getFacetsAndChildren();
        while (childrens.hasNext() /* && (result == null)*/) {
            children = (UIComponent) childrens.next();
            if (id.equals(children.getId())) {
                result = children;
                break;
            }
            result = findComponent(children, id);
            if (result != null) {
                break;
            }
        }
        return result;
    }


}
