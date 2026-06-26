package com.alerthub.userms.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.List;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString(exclude = "users")
@Entity
@Table(name = "roles")
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String role;

    @JsonIgnore
    @ManyToMany(mappedBy = "roles")
    private List<User> users;

    public Role(String role) {
        this.role = role;
    }
}
