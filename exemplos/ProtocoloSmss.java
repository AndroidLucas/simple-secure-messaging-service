package exemplos;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.HashMap;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

import protocols.ProtocolModel;
import util.Util;

/**
 * Suponha que o protocolo deve trocar 4 mensagens.
 * Par_req = {tipo, origem, destino, algoritmo, modo, padding}
 * Par_conf = {tipo, código, vetor de inicialização}
 * Dados = {tipo, código, tamanho, texto criptografado}
 * Conf = {tipo, código}
 * 
 * Código de erro: 0 para sucesso. Para demais códigos de erro e suas especificações, consultar a especificação do projeto.
 */
public class ProtocoloSmss extends ProtocolModel {
    /* inicializa variáveis a serem utilizadas na execução */
	private int valor = 0;
    private int errorCode = 0;
    private boolean errorMsg = false;
    private Cipher cipher;
    
    private String texto = "Texto nao obtido ainda";
    private byte[] initializationArray;
	private byte[] textoCriptografado = ("Texto criptografado nao obtido ainda").getBytes();

    /* inicializa dados do protocolo smss: */
    private int origem = 43563;
    private int destino = 43542;
    private int tipo = 0;
    private int algoritmo = -1;
    private int padding = -1;
    private int modo = -1;
    
    public Util util;
    
    
    /**
     * Servidor e cliente no modo padrão.
     * 
     * @param mode
     */
    ProtocoloSmss(int mode) {
        // gera um protocolo com 4 mensagens, alternando entre enviar e receber
        // Modo cliente, inicia com enviar, e modo servidor inicia com receber
        super(mode, 4);
    }
    
    /**
    * Este metodo deve retornar as mensagens exatamente como especificado
    * pelo protocolo. 
    * Cliente gera as mensagens: 0 e 2
    * Servidor gera as mensagens: 1 e 3
    * Todas as mensagens geradas são implementadas nesta classe, nos
    * respectivos métodos privados gen??Message()
    */
    protected byte[] nextMessage() {
        byte[] msg = null;
        switch (this.step) {
            case 0:
                //First message, generated by the CLIENT
            	// Par_req
                msg = this.genFirstMessage();
                System.out.println("Primeira mensagem enviada...");
                break;
            case 1:
                //Second message, genereted by the SERVER
                msg = this.genSecondMessage();
                System.out.println("Segunda mensagem enviada...");
                break;
            case 2:
                //Third message, generated by the CLIENT
                msg = this.genThirdMessage();
                System.out.println("Terceira mensagem enviada...");
                break;
            case 3:
                //Third message, generated by the CLIENT
                msg = this.genForthMessage();
                System.out.println("Quarta mensagem enviada...");
                break;
        }
        
        return msg;
    }

    /**
    * Este método valida se as mensagens recebidas são exatamente como
    * especificado pelo protocolo. 
    * Cliente valida as mensagens: 1 e 3
    * Servidor valida as mensagens: 0 e 2
    * Todas as validações de mensagens são implementadas nesta classe, nos
    * respectivos métodos privados verify??Message()
    */
    protected boolean verifyMessage(byte[] receivedMsg) {
        boolean ret = false;
        switch (this.step) {
            case 0:
                //Verify First message, performed by the SERVER
                ret = this.verifyFirstMessage(receivedMsg);
                System.out.println("Primeira mensagem ... " + ret);
                break;
            case 1:
                //Verify Second message, performed by the CLIENT
                ret = this.verifySecondMessage(receivedMsg);
                System.out.println("Segunda mensagem ... " + ret);
                break;
            case 2:
                //Verify Third message, performed by the SERVER
                ret = this.verifyThirdMessage(receivedMsg);
                System.out.println("Terceira mensagem ... " + ret);
                break;
            case 3:
                //Verify Third message, performed by the SERVER
                ret = this.verifyForthMessage(receivedMsg);
                System.out.println("Terceira mensagem ... " + ret);
                break;
        }
        
        return ret;
    }

    private byte[] genFirstMessage(){
        // definição do tamanho da mensagem
        byte[] ret = new byte[7];
        
        // atribuição correta dos bytes da mensagem
        // segue o padrão {tipo, origem, destino, algoritmo, modo, padding}
        
        ret[0] = (byte) this.tipo;
        ret[1] = (byte) (this.origem >> 8);
        ret[2] = (byte) (this.origem);
        ret[3] = (byte) (this.destino >> 8);
        ret[4] = (byte) (this.destino);
        ret[5] = (byte) ((this.algoritmo << 4 ) | (this.padding));
        ret[6] = (byte) (this.modo);
        
        return ret;
    }
    
    private byte[] genSecondMessage(){
        // definição do tamanho da mensagem
        byte[] ret = new byte[17];
        int erro = 0;
        
        // atualização dos parametros internos do protocol
        this.valor = this.valor+1;
        
        
     	// cria o vetor de inicialização
     	byte[] initializationArray = new byte[16];
        
		try	{
	     	SecureRandom srandom = new SecureRandom();
			srandom.nextBytes(initializationArray);
			
			// copiar o IV para o retorno
			System.arraycopy(initializationArray, 0, ret, 1, 16);
			
			this.initializationArray = initializationArray;
		}
		catch (Exception e) {
			erro = 2;
		}
		
		this.initializationArray = initializationArray;
		
		// atribuição correta dos bytes da mensagem
        // segue o padrão {tipo, código, vetor de inicialização}
		
		// monta retorno parConf:
        ret[0] =  (byte) ((this.step << 4 ) | (erro));
     				
        return ret;
    }
    
    private byte[] genThirdMessage(){    	
    	String mensagemDeTexto = this.texto;
    	
    	int tamanhoTexto = mensagemDeTexto.length();
    	
    	int i = 0;
    	int erro = 0;

    	// no código abaixo a mensagem é criptografada
    	byte[] criptografadaBytes = new byte[tamanhoTexto];
    	
    	try {
    		criptografadaBytes = mensagemDeTexto.getBytes("UTF-8");
    		criptografadaBytes = this.encrypt(criptografadaBytes);
		} catch (UnsupportedEncodingException e) {
			erro = 2;
		}
    	
    	this.setMensagemCriptografada(Util.byteToString(criptografadaBytes));
        
        // prenchimento da mensagem "Dados"
    	// atribuição correta dos bytes da mensagem
        // segue o padrão {tipo, código, tamanho, texto criptografado}
    	
        // definição do tamanho da mensagem
        byte[] ret = new byte[3 + tamanhoTexto];
        
        // código de erro e tipo da mensagem no primeiro byte:
        ret[0] = (byte) ((this.tipo << 4) | erro);

        // tamanho da mensagem nos bytes 1 e 2:
        ret[1] = (byte) (mensagemDeTexto.length() >> 8);
        ret[2] = (byte) (mensagemDeTexto.length());
		
        // copia a mensagem criptografa para o retorno
        int j = 3;
        for (i = 0; i < tamanhoTexto; i++) {
        	ret[j] = criptografadaBytes[i];
        	j++;
        } 
        
        return ret;
    }
    
    private byte[] genForthMessage(){
        // definição do tamanho da mensagem
    	byte[] ret = new byte[1];
    	byte[] criptografada = this.textoCriptografado;
    	
    	this.valor = this.valor+1;
    	boolean descriptografia =  true;
    	
    	// descriptografa a mensagem
    	String descriptografada = this.decrypt(criptografada);
    	
    	// adicionar validacao: mensagem o tipo 4 (Confirma recepção dos dados e finaliza conexão)
    	if(descriptografia) {
    		this.setMensagem(descriptografada);
    		ret[0] = (byte) ((4 << 4) | 0);
    	}

    	return ret;
    }
    
    private boolean verifyFirstMessage(byte[] msg){
    	// separa as informações obtidas com a mensagem recebida
    	this.algoritmo = ( msg[5] >> 4);
    	this.padding = (msg[5] & 15);
    	this.tipo = 1;
    	this.modo = (int) msg[6];
    	
    	int error = (msg[0] & 15);
    	
    	// verifica se apresentou erro
    	if (error == 0) {
    		// verifica se os parâmetros de criptografia são aceitos
	    	if (this.validateParams()) {
		    	System.out.println("[Par_req] algoritmo: " + this.algoritmo + ", padding: " + this.padding + ", tipo: " 
		    						+ this.tipo + " , modo: " + this.modo);
		    	
		        // atualização do status do protocolo, para o caso de enviar mensagem de erro
		        this.errorMsg = false;
		        // atualiza o estado das informações recebidas
		        this.valor = (int)msg[0];
		        this.tipo = (int)msg[0];
		        
		        return true;
	    	}
	    	else {
	    		this.setErrorCode(1);
	    		// Código de erro 1: Os parâmetros de criptografia não são suportados
	    		System.out.println("ERRO: 1");
	    		return false;
	    	}
    	}
    	else {
    		System.out.println("ERRO: " + error);
    		return false;
    	}
    }

    private boolean verifySecondMessage(byte[] msg){
    	int i,j;
		byte[] initializationArray = new byte[16];
    	
    	// mensagem, provavelmente, correta
        boolean ret = true;
        this.errorMsg = false;
        
        // decompõe mensagem nos parâmetros do protocolo para validação
        int tipo = (int) (msg[0] >> 4);
        int erro = (msg[0] & 15);
        

        // analisa se mensagem recebida é valida (código de erro 0) ou não
        if( erro != 0) {
        	System.out.println("[Par_conf] Tipo: " + tipo + ", Erro: " + erro );
        	
        	this.errorMsg = true;
        	ret = false;
        	this.setErrorCode(1);
        }
        else {
	        this.tipo = tipo;
	        
	        // copia o IV da mensagem recebida para o atributo da classe
    		System.arraycopy(msg, 1, initializationArray, 0, 16);
    		this.initializationArray = initializationArray;
			
			System.out.println("[Par_conf] Tipo: " + tipo + ", Erro: " + this.errorCode + ", IV: " + Arrays.toString(this.initializationArray));
        }
		
		return ret;
    }
    
    private boolean verifyThirdMessage(byte[] msg){
        int i,j = 0;
        // definição do tamanho da mensagem
        int tamanhoMsg = msg.length;
        
        // tamanho - 3 para ignorar os demais bytes que não se referem à mensagem criptografada
        byte[] bytesMensagemCriptografada = new byte[tamanhoMsg-3];
        
        // abaixo, a mensagem criptograda é salva em atributo da classe
    	for (i=3;i<tamanhoMsg; i++) {
    		bytesMensagemCriptografada[j] = msg[i];
    		j++;
    	}
    	
    	this.textoCriptografado = bytesMensagemCriptografada;
    	this.texto = bytesMensagemCriptografada.toString();
    	
    	return true;
    }
    
    private boolean verifyForthMessage(byte[] msg){
    	boolean ret = false;
    	
    	// separa as informações obtidas com a mensagem recebida
    	this.tipo = ( msg[0] >> 4);
    	int erro = (msg[0] & 15);
    	
    	// adicionar validacao de tipo e tratamento de erro
    	if(erro == 0) {
    		ret = true;
    	}
    	
        return ret;
    }
    
    /**
     * Este método informa ao RunProtocol o tamanho do cabeçalho
     * para leitura
     * @return 
     */
    public int getHeaderLenght() {
        int ret = 0;
        // Precisa reconhecer o tipo da mensagem e saber o tamanho
        // do cabe�alho e payload para leitura pelo RunProtocol
        switch (this.getStep()) {
            case 0: ret = 1;break;
            case 1: ret = 1;break;
            case 2: ret = 3;break;
            case 3: ret = 2;break;
        }
        
        return ret;
    }

    /**
     * Este método informa ao RunProtocol o tamanho do payload
     * Em mensagens de tamanho variável esta informação está no cabeçalho
     * @param header
     * @return 
     */
    public int getPayloadLenght(byte[] header) {
        int ret = 0;
        // precisa reconhecer o tipo da mensagem e saber o tamanho
        // do cabe�alho e payload 
        
        switch (this.getStep()) {
            case 0: ret = 6;break;
            case 1: ret = 16;break;
            case 2: ret =  ((header[1] << 4 ) | (header[2]));
                    //System.out.println("Header: "+header[1]);
                    break;
            case 3: ret = 1;break;
        }
        return ret;
    }
    
    /**
     * Recupera mensagem salva na classe
     * @return
     */
    public String getMensagem(){
        return this.texto;
    }
    
    /**
     * Seta valor da mensagem salva na classe
     * @return
     */
    public void setMensagem(String texto){
    	this.texto = texto;
    }
    
    /**
     * Seta valor da mensagem criptografada salva na classe
     * @return
     */
    public void setMensagemCriptografada(String texto){
        if (this.mode == SERVER) {
            this.textoCriptografado = texto.getBytes();
        }
    }
    
    /**
     * Seta o algoritmo a ser utilizado
     * @return
     */
    public void setAlgoritmo(byte algoritmo) {
    	if (this.mode == CLIENT) {
            this.algoritmo = algoritmo;
        }
    }
    
    /**
     * Seta o padding a ser utilizado
     * @return
     */
    public void setPadding(byte padding) {
    	if (this.mode == CLIENT) {
            this.padding = padding;
        }
    }
    
    /**
     * Seta o modo a ser utilizado
     * @return
     */
    public void setModo(int modo) {
    	if (this.mode == CLIENT) {
            this.modo = modo;
        }
    }
    
    /**
     * Seta o código de erro
     * @return
     */
    public void setErrorCode(int error) {
    	this.errorCode = error;
    }

    public static final HashMap<Integer, String> algMap = new HashMap<Integer, String>();
	{
		// Algoritmos aceitos
		algMap.put(0, "AES"); // AES com chave de 128 bits
		algMap.put(1, "AES"); // AES com chave de 192 bits
		algMap.put(2, "AES"); // AES com chave de 256 bits
	};
	public static final HashMap<Integer, String> modeMap = new HashMap<Integer, String>();
	{
		// Modo aceito
		modeMap.put(5, "CFB128"); // Cipher FeedBack com deslocamento de 16 bytes (S)
	};
	public static final HashMap<Integer, String> padMap = new HashMap<Integer, String>();
	{
		// Padding aceito
		padMap.put(0, "NoPadding"); // Sem preenchimento
	};
	
	/**
     * Verifica se os padrões de criptografia são aceitos
     * @return
     */
	public boolean validateParams() {
		// verifica algoritmo
		if (!algMap.containsKey(this.algoritmo)) {
			return false;
		}
		// verifica modo
		if (!modeMap.containsKey(this.modo)) {
			return false;
		}
		// verifica padding
		if (!padMap.containsKey(this.padding)) {
			return false;
		}
		
		return true;
	}

	/**
     * Retorna a instância de criptografia a ser utilizada
     * @return
     */
	public String cryptoInstance() {
		String ret;

		ret = algMap.get(new Integer(this.algoritmo)) + "/" + modeMap.get(new Integer(this.modo)) + "/"
				+ padMap.get(new Integer(this.padding));
		return ret;
	}
	
	/**
     * Criptografa a mensagem
     * @return
     */
	public byte[] encrypt(byte[] msg) throws UnsupportedEncodingException {
		IvParameterSpec ivspec = new IvParameterSpec(this.initializationArray);
		
		try {
			// define a chave
			String key = Util.stringToHexString("chave 1 de teste");
			SecretKeySpec skeyspec = new SecretKeySpec(key.getBytes(), "AES");
			
			// instancia cipher
			Cipher ci = Cipher.getInstance(this.cryptoInstance());
			this.cipher = ci;
			this.cipher.init(Cipher.ENCRYPT_MODE, skeyspec, ivspec);
			
			// criptografa
			byte[] encoded = this.cipher.doFinal(msg);
			
			return encoded;
			
		} catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException e) {
			// ERRO
			return e.toString().getBytes("UTF-8");
		}
	}
	
	/**
     * Descriptografa a mensagem
     * @return
     */
	public String decrypt(byte[] encoded) {
		try {
			// define a chave
			String key = Util.stringToHexString("chave 1 de teste");
			SecretKeySpec skeyspec = new SecretKeySpec(key.getBytes(), "AES");
			
			IvParameterSpec ivspec = new IvParameterSpec(this.initializationArray);
			
			// instancia cipher
			Cipher ci = Cipher.getInstance(this.cryptoInstance());
			this.cipher = ci;
			ci.init(Cipher.DECRYPT_MODE, skeyspec, ivspec);
			
			// descriptografa
			String decoded = new String(this.cipher.doFinal(encoded), "UTF-8");
			
			return decoded;
		} catch (UnsupportedEncodingException | IllegalBlockSizeException | BadPaddingException | InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException | InvalidAlgorithmParameterException e) {
			// ERRO
			return e.toString();
		}
	}
}
