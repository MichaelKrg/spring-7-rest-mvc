package guru.springframework.spring7restmvc.repositories;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import guru.springframework.spring7restmvc.entities.Category;

public interface CategoryRepository extends JpaRepository<Category, UUID>{

}
