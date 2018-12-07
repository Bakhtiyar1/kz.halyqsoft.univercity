package kz.halyqsoft.univercity.filter.panel;

import com.vaadin.ui.AbstractField;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.HorizontalLayout;
import kz.halyqsoft.univercity.filter.FEmployeeFilter;
import kz.halyqsoft.univercity.modules.schedule.ScheduleEmployee;
import org.r3a.common.vaadin.widget.filter2.panel.AbstractFilterPanel;

/**
 * @author Omarbek
 * @created Apr 10, 2018 5:25:59 PM
 */
@SuppressWarnings("serial")
public final class EmployeeFilterPanel extends AbstractFilterPanel {

	public EmployeeFilterPanel(FEmployeeFilter filterBean) {
		super(filterBean);
	}

	@Override
	protected void initWidget() {
		HorizontalLayout hl = new HorizontalLayout();
		hl.setSpacing(true);
		hl.setWidthUndefined();
		
		AbstractField af = getFilterComponent("code");
		if (af != null) {
			hl.addComponent(af);
		}

		af = getFilterComponent("card");
		if (af != null) {
			hl.addComponent(af);
		}

		af = getFilterComponent("firstname");
		if (af != null) {
			hl.addComponent(af);
		}

		af = getFilterComponent("lastname");
		if (af != null) {
			hl.addComponent(af);
		}

		getContent().addComponent(hl);
		getContent().setComponentAlignment(hl, Alignment.MIDDLE_CENTER);

		hl = new HorizontalLayout();
		hl.setSpacing(true);
		hl.setWidthUndefined();
		
		af = getFilterComponent("department");
		if (af != null) {
			hl.addComponent(af);
		}
		
		af = getFilterComponent("post");
		if (af != null) {
			hl.addComponent(af);
		}

		af = getFilterComponent("childAge");
		if (af != null) {
			hl.addComponent(af);
		}

		af = getFilterComponent("subject");
		if (af != null) {
			hl.addComponent(af);
		}

		af = getFilterComponent("childCount");
	   	if (af != null) {
			hl.addComponent(af);
		}

		getContent().addComponent(hl);
		getContent().setComponentAlignment(hl, Alignment.MIDDLE_CENTER);
	}
}
