package com.catan.spring.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import catan.models.Aresta;

public interface ArestaRepository extends JpaRepository<Aresta, Integer> {
}
