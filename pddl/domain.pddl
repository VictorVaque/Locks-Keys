(define (domain joc_de_puzle_agentes)
  (:requirements :strips :typing :negative-preconditions)

  (:types
    agent location key door
  )

  (:predicates
    ;; Estado del agente y el mapa
    (at ?a - agent ?l - location)
    (occupied ?l - location)
    (connected ?l1 - location ?l2 - location)

    ;; Propiedades de las ubicaciones (mutuamente exclusivas)
    (is-empty ?l - location)
    (is-wall ?l - location)
    (is-key ?l - location ?k - key)
    (is-door ?l - location ?d - door)
    (is-exit ?l - location)

    ;; Relaciones y estado del juego
    (opens ?k - key ?d - door)
    (agent-has-key ?k - key)
    (solved)
  )


  (:action move
    :parameters (?a - agent ?from - location ?to - location)
    :precondition (and
      (at ?a ?from)
      (connected ?from ?to)
      (is-empty ?to)
      (not (occupied ?to))
    )
    :effect (and
      (not (at ?a ?from))
      (at ?a ?to)
      (not (occupied ?from))
      (occupied ?to)
      (not (is-empty ?to))
      (is-empty ?from)
    )
  )


  (:action pick-key
    :parameters (?a - agent ?from - location ?to - location ?k - key)
    :precondition (and
      (at ?a ?from)
      (connected ?from ?to)
      (is-key ?to ?k)
      (not (occupied ?to))
    )
    :effect (and
      (not (at ?a ?from))
      (at ?a ?to)
      (not (occupied ?from))
      (occupied ?to)
      (not (is-key ?to ?k))
      (agent-has-key ?k)
      (is-empty ?from)
    )
  )


  (:action cross-door
    :parameters (?a - agent ?from - location ?to - location ?d - door ?k - key)
    :precondition (and
      (at ?a ?from)
      (connected ?from ?to)
      (is-door ?to ?d)
      (opens ?k ?d)
      (agent-has-key ?k)
      (not (occupied ?to))
    )
    :effect (and
      (not (at ?a ?from))
      (at ?a ?to)
      (not (occupied ?from))
      (occupied ?to)
      (is-empty ?from)
    )
  )


  (:action reach-exit
    :parameters (?a - agent ?from - location ?to - location)
    :precondition (and
      (at ?a ?from)
      (connected ?from ?to)
      (is-exit ?to)
      (not (occupied ?to))
    )
    :effect (and
      (not (at ?a ?from))
      (at ?a ?to)
      (not (occupied ?from))
      (occupied ?to)
      (is-empty ?from)
      (solved)
    )
  )
)
