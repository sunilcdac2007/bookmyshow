package bookmyshow.gateway.authservice.entity;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;
import java.util.Set;


@Entity
@Table(name = "user")
@Getter
@Setter
public class User implements Serializable {

    private static final long serialVersionUID = -4163176478323743270L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_key")
    private Long objectId;
    @Column(name = "user_code")
    private String userCode;
    @Column(name = "user_name")
    private String userName;
    @Column(name = "gender")
    private String gender;

    @Column(name = "user_status")
    private String userStatus;
    @Column(name = "ldap_user_dn")
    private String ldapUserDn;
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
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_type_key", nullable = false)
    private UserType userType;
    @Column(name = "user_email_id")
    private String userEmailId;
    @Column(name = "user_password")
    private String userPassword;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_contact_key", nullable = false)
    private Contact contactEntity;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_org_key", nullable = false)
    private Organization organizationEntity;
}
