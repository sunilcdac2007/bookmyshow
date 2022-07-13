package bookmyshow.gateway.authservice.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "contact")
@Getter
@Setter
public class Contact implements Serializable {
    private static final long serialVersionUID = 5055389832096139912L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "contact_key")
    private Long objectId;
    @Column(name = "name")
    private String name;
    @Column(name = "`vtc`")
    private String vtc;
    @Column(name = "district")
    private String district;
    @Column(name = "sub_district")
    private String subDistrict;
    @Column(name = "state")
    private String state;
    @Column(name = "pincode")
    private String pincode;
    @Column(name = "phone1")
    private String phone1;
    @Column(name = "phone2")
    private String phone2;
    @Column(name = "fax")
    private String fax;
    @Column(name = "email_id1")
    private String emailId1;
    @Column(name = "email_id2")
    private String emailId2;
    @Column(name = "addressLine1")
    private String addressLine1;
    @Column(name = "addressLine2")
    private String addressLine2;
    @Column(name = "created_by")
    private String createdBy;
    @Column(name = "last_updated_by")
    private String lastUpdatedBy;
    @Column(name = "creation_date")
    private Date creationDate;
    @Column(name = "last_updated_date")
    private Date lastUpdatedDate;
}
