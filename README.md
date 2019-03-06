<a href="http://mcm.ipg.pt"><img src="http://www.ipg.pt/website/imgs/logotipo_ipg.jpg" title="IPG(MCM)" alt="IPG MCM 2018/19"></a>

# API end Point com Mongodb
## Documentação
https://documenter.getpostman.com/view/1885494/S11Bxgv2#6bf6dd63-7868-4b61-9859-1da9983f0bec

### Chat OK entre vários utilizadores (Guarda histórico na base dados MongoDB com API):

1: fazer registo (Ainda só na API) :http://chat-ipg-04.azurewebsites.net/api/auth/register

2: criar contactos (Ainda Só na API) : http://chat-ipg-04.azurewebsites.net/api/chat/new/"ID_do_utilizador_a_adicionar" (deve ser colocar JWT token de autenticação)
 
2: abrir Android chat e fazer login com email+password do registo

3: ver que o contacto já está criado

4: enviar mensagens encriptadas entre utilizadores

### TODO
# Functionality

### Funcionalidades - Serviços
- [x] Troca de texto
- [ ] Suporte de voz
- [ ] Troca de ficheiros
- [x] Envio localização GPS

### Requisitos de Modos de comunicação
- [ ] Comunicação por infra-estrutura
- [X] Utilização de rede wifi ou Ethernet para comunicar
- [ ] Comunicação Standalone
- [ ] Estabelecimento de comunicação via Bluetooth com os dispositivos que são detectados

### Requisitos de implementação
- [x] Gestão de utilizadores em servidor central, com sinalização de utilizadores online/offline
- [x] Armazenamento de mensagem em servidor para possibilidade concorrente em vários dispositivos
- [ ] Comunicação em claro ou encriptada por selecção explicita da encriptação por parte dos utilizadores

### Requisitos de implementação - Segurança
- [X] Comunicação em claro com verificação da integridade da comunicação
- [ ] Escolha de um dos algoritmos MD5 ou SHA Hash Algorithms: SHA-1, HAVAL, MD2, MD5, SHA-256, SHA-384, SHA-512
- [x] Comunicação encriptada de mensagens
- [ ] Escolha de um dos protocolos DES, 3DES ou AES


## Version
v0.6

![1](https://user-images.githubusercontent.com/2634610/53902629-6e56b300-4039-11e9-8a4d-6931c7688f27.png)
![2](https://user-images.githubusercontent.com/2634610/53902626-6dbe1c80-4039-11e9-9e1c-4fca8d6f417c.png)

v0.5

![screenshot_20190306-172509](https://user-images.githubusercontent.com/2634610/53901257-76f9ba00-4036-11e9-8354-7c47436ce9cf.png)

v0.2

![1_chat](https://user-images.githubusercontent.com/2634610/53040183-bb9f2600-3478-11e9-94c1-e95d8fafb17e.png)
![2_chat](https://user-images.githubusercontent.com/2634610/53040184-bb9f2600-3478-11e9-8bed-4a94e5375017.png)
![3_chat](https://user-images.githubusercontent.com/2634610/53040187-bc37bc80-3478-11e9-91d1-a1d70be1f752.png)




### Contributing

