package kz.halyqsoft.univercity.entity.beans.univercity;

import kz.halyqsoft.univercity.entity.beans.USERS;
import kz.halyqsoft.univercity.entity.beans.univercity.catalog.PDF_DOCUMENT_TYPE;
import org.r3a.common.entity.AbstractEntity;
import org.r3a.common.entity.EFieldType;
import org.r3a.common.entity.FieldInfo;

import javax.persistence.*;
import java.util.Date;

@Entity
public class PDF_DOCUMENT extends AbstractEntity {

    @FieldInfo(type = EFieldType.FK_COMBO, inGrid = false, inEdit = false, inView = false, order = 1)
    @ManyToOne
    @JoinColumns({
            @JoinColumn(name = "USER_ID", referencedColumnName = "ID", nullable = false)})
    private USERS user;

    @FieldInfo(type = EFieldType.TEXT, order = 2, inGrid = false , inEdit = false , inView = false)
    @Column(name = "title")
    private String title;

    @FieldInfo(type = EFieldType.TEXT, order = 3)
    @Column(name = "file_name", nullable = false)
    private String fileName;

    @FieldInfo(inGrid = false , inView = false , inEdit = false, order = 4)
    @Column(name = "deleted", nullable = false)
    private Boolean deleted;

    @FieldInfo(type = EFieldType.DATETIME, inView = true ,inEdit = false , inGrid = true , order = 5)
    @Column(name = "created")
    @Temporal(TemporalType.TIMESTAMP)
    private Date created;

    @FieldInfo( inEdit = false, order = 6)
    @Column(name = "period")
    private int period;

    @FieldInfo(type = EFieldType.BOOLEAN,inEdit = false,  order = 7)
    @Column(name = "for_human_resource_department")
    private boolean forHumanResourceDepartment = false;

    @FieldInfo(type = EFieldType.FK_COMBO,inEdit = true,  order = 8,required = false)
    @ManyToOne
    @JoinColumns({@JoinColumn(name = "PDF_DOCUMENT_TYPE_ID", referencedColumnName = "ID", nullable = true)})
    private PDF_DOCUMENT_TYPE pdfDocumentType;

    public PDF_DOCUMENT() {
    }

    public USERS getUser() {
        return user;
    }

    public void setUser(USERS user) {
        this.user = user;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public Boolean getDeleted() {
        return deleted;
    }

    public void setDeleted(Boolean deleted) {
        this.deleted = deleted;
    }

    public int getPeriod() {
        return period;
    }

    public void setPeriod(int period) {
        this.period = period;
    }

    @Override
    public String toString() {
        return fileName ;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public boolean isForHumanResourceDepartment() {
        return forHumanResourceDepartment;
    }

    public void setForHumanResourceDepartment(boolean forHumanResourceDepartment) {
        this.forHumanResourceDepartment = forHumanResourceDepartment;
    }

    public PDF_DOCUMENT_TYPE getPdfDocumentType() {
        return pdfDocumentType;
    }

    public void setPdfDocumentType(PDF_DOCUMENT_TYPE pdfDocumentType) {
        this.pdfDocumentType = pdfDocumentType;
    }
}
