package com.server.app.entities;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Table(name = "roles")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Builder
public class Role {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(
          unique = true,
          nullable = false,
          length = 50
  )
  private String name;

  @Column(nullable = false)
  @Builder.Default
  private Boolean active = true;

  @ManyToMany(
          fetch = FetchType.EAGER,
          cascade = {
                  CascadeType.PERSIST,
                  CascadeType.MERGE
          }
  )
  @JoinTable(
          name = "role_permissions",
          joinColumns = @JoinColumn(name = "role_id"),
          inverseJoinColumns = @JoinColumn(name = "permission_id")
  )
  @EqualsAndHashCode.Exclude
  @ToString.Exclude
  @Builder.Default
  private Set<Permission> permissions = new HashSet<>();
}