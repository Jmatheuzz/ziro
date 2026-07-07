package com.ziro.email;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${ziro.frontend-url}")
    private String frontendUrl;

    public void enviarEmailVerificacao(String destinatario, String nome, String codigo) {
        String link = frontendUrl + "/verificar-email?codigo=" + codigo;

        SimpleMailMessage mensagem = new SimpleMailMessage();
        mensagem.setTo(destinatario);
        mensagem.setSubject("Confirme seu email - Ziro");
        mensagem.setText("""
                Oi, %s!

                Falta pouco pra comecar a usar o Ziro. Confirme seu email clicando no link abaixo:

                %s

                Se voce nao criou uma conta no Ziro, pode ignorar esse email.
                """.formatted(nome, link));

        mailSender.send(mensagem);
    }

    public void enviarEmailRecuperacaoSenha(String destinatario, String nome, String codigo) {
        String link = frontendUrl + "/redefinir-senha?codigo=" + codigo;

        SimpleMailMessage mensagem = new SimpleMailMessage();
        mensagem.setTo(destinatario);
        mensagem.setSubject("Recuperacao de senha - Ziro");
        mensagem.setText("""
                Oi, %s!

                Recebemos um pedido pra redefinir sua senha no Ziro. Clique no link abaixo pra criar uma nova senha:

                %s

                Se voce nao pediu isso, pode ignorar esse email - sua senha continua a mesma.
                """.formatted(nome, link));

        mailSender.send(mensagem);
    }

    public void enviarEmailConviteEquipe(String destinatario, String nomeConvidado, String nomeEmpresa, String codigo) {
        String link = frontendUrl + "/redefinir-senha?codigo=" + codigo;

        SimpleMailMessage mensagem = new SimpleMailMessage();
        mensagem.setTo(destinatario);
        mensagem.setSubject("Voce foi convidado pra " + nomeEmpresa + " - Ziro");
        mensagem.setText("""
                Oi, %s!

                Voce foi convidado pra fazer parte da equipe de %s no Ziro.
                Clique no link abaixo pra criar sua senha e comecar a usar o sistema:

                %s

                Se voce nao esperava esse convite, pode ignorar esse email.
                """.formatted(nomeConvidado, nomeEmpresa, link));

        mailSender.send(mensagem);
    }
}
