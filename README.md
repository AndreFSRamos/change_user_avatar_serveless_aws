# change_user_avatar_serveless_aws

## Controller
Classe responsável por disponibilizar as rotas de API Rest, nela estão disponíveis os endpoints:
- GET: user/{id}/avatar;
- DELETE: user/{id}/avatar;

## Services
Classes responsáveis por manter a lógica da aplicação:
- AvatarService: contém a lógica para geração e exclusão de avatares;
- S3Service: contém a lógica para acessar os métodos da SE;

## Configs
Contém configurações necessárias para a AWS;

## Handlers
Contém a classe handlers necessária para trabalhar com o serviço lambda da AWS, ela basicamente faz ponte entre os eventos AWS e a controller da aplicação.


### A classe controller não se faz necessária, poderia muito bem acessar a camada "Services" diretamente.
