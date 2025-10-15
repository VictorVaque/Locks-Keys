(define (problem mapa_1)
  (:domain joc_de_puzle_agentes)

  (:objects
    agent1 agent2 - agent
    x0y0 x1y0 x2y0 x3y0 x4y0 x5y0 x6y0 x7y0
    x0y1 x1y1 x2y1 x3y1 x4y1 x5y1 x6y1 x7y1
    x0y2 x1y2 x2y2 x3y2 x4y2 x5y2 x6y2 x7y2
    x0y3 x1y3 x2y3 x3y3 x4y3 x5y3 x6y3 x7y3
    x0y4 x1y4 x2y4 x3y4 x4y4 x5y4 x6y4 x7y4 - location
    key_a - key
    door_A - door
  )

  (:init
    ;; Estado inicial de los agentes
    (at agent1 x4y3) (at agent2 x2y1)
    (occupied x4y3) (occupied x2y1)

    ;; Propiedades de las ubicaciones
    (is-wall x0y0) (is-wall x1y0) (is-wall x2y0) (is-wall x3y0) (is-wall x4y0) (is-wall x5y0) (is-wall x6y0) (is-wall x7y0)
    (is-wall x0y1) (is-wall x6y1) (is-wall x7y1)
    (is-wall x0y2) (is-wall x5y2) (is-wall x7y2)
    (is-wall x0y3) (is-wall x7y3)
    (is-wall x0y4) (is-wall x1y4) (is-wall x2y4) (is-wall x3y4) (is-wall x4y4) (is-wall x5y4) (is-wall x6y4) (is-wall x7y4)

    (is-key x1y2 key_a)
    (is-door x6y3 door_A)
    (is-exit x6y2)

    ;; Declaración de casillas vacías
    (is-empty x1y1)
    (is-empty x3y1) (is-empty x4y1) (is-empty x5y1)
    (is-empty x2y2) (is-empty x3y2) (is-empty x4y2)
    (is-empty x1y3) (is-empty x2y3) (is-empty x3y3) (is-empty x5y3)
    (is-empty x5y4)

    ;; Relación clave-puerta
    (opens key_a door_A)

    ;; Conexiones de la cuadrícula
    (connected x1y1 x2y1) (connected x2y1 x1y1)
    (connected x2y1 x3y1) (connected x3y1 x2y1)
    (connected x3y1 x4y1) (connected x4y1 x3y1)
    (connected x4y1 x5y1) (connected x5y1 x4y1)
    (connected x1y2 x2y2) (connected x2y2 x1y2)
    (connected x2y2 x3y2) (connected x3y2 x2y2)
    (connected x3y2 x4y2) (connected x4y2 x3y2)
    (connected x4y2 x5y2) (connected x5y2 x4y2)
    (connected x5y2 x6y2) (connected x6y2 x5y2)
    (connected x1y3 x2y3) (connected x2y3 x1y3)
    (connected x2y3 x3y3) (connected x3y3 x2y3)
    (connected x3y3 x4y3) (connected x4y3 x3y3)
    (connected x4y3 x5y3) (connected x5y3 x4y3)
    (connected x5y3 x6y3) (connected x6y3 x5y3)
    (connected x1y1 x1y2) (connected x1y2 x1y1)
    (connected x2y1 x2y2) (connected x2y2 x2y1)
    (connected x3y1 x3y2) (connected x3y2 x3y1)
    (connected x4y1 x4y2) (connected x4y2 x4y1)
    (connected x5y1 x5y2) (connected x5y2 x5y1)
    (connected x6y2 x6y3) (connected x6y3 x6y2)
    (connected x1y2 x1y3) (connected x1y3 x1y2)
    (connected x2y2 x2y3) (connected x2y3 x2y2)
    (connected x3y2 x3y3) (connected x3y3 x3y2)
    (connected x4y2 x4y3) (connected x4y3 x4y2)
    (connected x5y2 x5y3) (connected x5y3 x5y2)
  )

  (:goal (solved))
)
