# Fase 2

1. Criar uma topologia aleatória
    Feita pela classe [Graph](https://github.com/vepo/openjfx-graph/blob/master/openjfx-graph/src/main/java/dev/vepo/openjgraph/graph/Graph.java#L121)
2. Criar tabela de roteamento em cada nó para todos os destinos da rede com a menor rota (valor dos enlaces = 1)
    Feita pela classe [Roteamento](https://github.com/vepo/rsf0075-redes-sem-fio/blob/main/src/main/java/io/vepo/redes/Roteamento.java) 

Algoritmo:
    Cada nó irá enviar uma mensagem de RouteDiscovery contendo o nó original, o caminho percorrido e a distância a todos os seus vizinhos.
    Ao receber uma mensagem de RouteDiscovery e o nó não conhecer a origem ou a rota for menor que a conhecida, o nó deve atualizar sua tabela e enviar a mensagem a todos os vizinhos que não estejam no caminho percorrida da mensagem.
