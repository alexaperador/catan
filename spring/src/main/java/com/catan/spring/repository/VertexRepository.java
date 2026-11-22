package com.catan.spring.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import catan.models.Vertex;

public interface VertexRepository extends JpaRepository<Vertex, Integer> {
}
