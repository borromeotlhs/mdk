package gov.nasa.jpl.mbee.ems.validation.actions;

import gov.nasa.jpl.mbee.ems.ExportUtility;
import gov.nasa.jpl.mgss.mbee.docgen.validation.IRuleViolationAction;
import gov.nasa.jpl.mgss.mbee.docgen.validation.RuleViolationAction;

import java.awt.event.ActionEvent;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.nomagic.magicdraw.annotation.Annotation;
import com.nomagic.magicdraw.annotation.AnnotationAction;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;

public class ExportPropertyType extends RuleViolationAction implements AnnotationAction, IRuleViolationAction {


    private static final long serialVersionUID = 1L;
    private Element element;
    
    public ExportPropertyType(Element e) {
        //JJS--MDEV-567 fix: changed 'Export' to 'Commit'
        //
        super("ExportPropertyType", "Commit property type", null, null);
        this.element = e;
    }
    
    @Override
    public boolean canExecute(Collection<Annotation> arg0) {
        return true;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void execute(Collection<Annotation> annos) {
        JSONObject send = new JSONObject();
        JSONArray infos = new JSONArray();
        Set<Element> set = new HashSet<Element>();
        for (Annotation anno: annos) {
            Element e = (Element)anno.getTarget();
            set.add(e);
            JSONObject info = getInfo(e);
            infos.add(info);
        }
        if (!ExportUtility.okToExport(set))
            return;
        send.put("elements", infos);
        String url = ExportUtility.getPostElementsUrl();
        if (url == null) {
            return;
        }
        if (ExportUtility.send(url, send.toJSONString())) {
            this.removeViolationsAndUpdateWindow(annos);
            //ExportUtility.sendProjectVersions();
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void actionPerformed(ActionEvent e) {
        if (!ExportUtility.okToExport(element))
            return;
        JSONObject info = getInfo(element);
        JSONArray elements = new JSONArray();
        JSONObject send = new JSONObject();
        //if (element instanceof Property || element instanceof Slot)
        //    elements.addAll(ExportUtility.getReferencedElements(element).values());
        elements.add(info);
        send.put("elements", elements);
        String url = ExportUtility.getPostElementsUrl();
        if (url == null) {
            return;
        }
        if (ExportUtility.send(url, send.toJSONString())) {
            this.removeViolationAndUpdateWindow();
            //ExportUtility.sendProjectVersion(element);
        }

    }

    @SuppressWarnings("unchecked")
    private JSONObject getInfo(Element e) {
        JSONObject elementInfo = new JSONObject();
        elementInfo.put("specialization", ExportUtility.fillPropertySpecialization(e, null, true));
        elementInfo.put("sysmlid", ExportUtility.getElementID(e));
        return elementInfo;
    }
}