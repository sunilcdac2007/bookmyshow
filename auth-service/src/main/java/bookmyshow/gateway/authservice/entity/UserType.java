package bookmyshow.gateway.authservice.entity;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "user_type")
@Getter
@Setter
public class UserType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_type_key")
    private Long objectId;
    @Column(name = "user_type_code")
    private String userTypeCode;
    @Column(name = "user_type_desc")
    private String userTypeDesc;
    @Column(name = "user_type_status")
    private String userTypeStatus;
    @Column(name = "ldap_group_dn")
    private String ldapGroupDn;
    @Column(name = "created_by")
    private String createdBy;
    @Column(name = "last_updated_by")
    private String lastUpdatedBy;
    @Column(name = "creation_date")
    @Type(type = "timestamp")
    private Date creationDate;
    @Column(name = "last_updated_date")
    @Type(type = "timestamp")
    private Date lastUpdatedDate;
}
