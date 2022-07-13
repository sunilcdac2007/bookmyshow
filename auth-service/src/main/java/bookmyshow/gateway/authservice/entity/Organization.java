package bookmyshow.gateway.authservice.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.Table;
import java.io.Serializable;

@Entity
@Table(name = "organization")
@Getter
@Setter
public class Organization implements Serializable {
}
