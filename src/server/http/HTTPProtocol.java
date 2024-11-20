package server.http;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class HTTPProtocol implements Runnable {
	
	private Socket sock;
	
	private BufferedReader in;
	private PrintWriter out;
	
	public HTTPProtocol(Socket s) {
		sock = s;
	}
	
	public void run() {
		try {
			in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
			out = new PrintWriter(sock.getOutputStream());
		} catch (IOException ex) {
			return;
		}
		
		try {
			in.readLine();
		} catch (IOException ex) {
			return;
		}
		
		out.println("HTTP/1.1 200 OK");
		out.println("Content-Type: text/html");
		out.println("");
		
		out.println("<!DOCTYPE html>");
		out.println("<html lang=\"it\">");
		out.println("<head>");
		out.println("    <meta charset=\"UTF-8\">");
		out.println("    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">");
		out.println("    <meta http-equiv=\"X-UA-Compatible\" content=\"ie=edge\">");
		out.println("    <title>Benvenuto nel nostro sito!</title>");
		out.println("    <style>");
		out.println("        /* CSS per il layout e lo stile della pagina */");
		out.println("        body {");
		out.println("            font-family: 'Arial', sans-serif;");
		out.println("            background-color: #f4f7fc;");
		out.println("            margin: 0;");
		out.println("            padding: 0;");
		out.println("            display: flex;");
		out.println("            justify-content: center;");
		out.println("            align-items: center;");
		out.println("            height: 100vh;");
		out.println("            color: #333;");
		out.println("            text-align: center;");
		out.println("        }");
		out.println("");
		out.println("        .container {");
		out.println("            background-color: #ffffff;");
		out.println("            border-radius: 10px;");
		out.println("            box-shadow: 0 4px 10px rgba(0, 0, 0, 0.1);");
		out.println("            padding: 40px;");
		out.println("            max-width: 600px;");
		out.println("            width: 100%;");
		out.println("            animation: fadeIn 1s ease-in-out;");
		out.println("        }");
		out.println("");
		out.println("        h1 {");
		out.println("            font-size: 2.5rem;");
		out.println("            color: #4CAF50;");
		out.println("            margin-bottom: 20px;");
		out.println("        }");
		out.println("");
		out.println("        p {");
		out.println("            font-size: 1.1rem;");
		out.println("            color: #555;");
		out.println("            margin-bottom: 30px;");
		out.println("        }");
		out.println("");
		out.println("        .btn {");
		out.println("            background-color: #4CAF50;");
		out.println("            color: white;");
		out.println("            padding: 12px 25px;");
		out.println("            font-size: 1rem;");
		out.println("            border: none;");
		out.println("            border-radius: 5px;");
		out.println("            cursor: pointer;");
		out.println("            text-decoration: none;");
		out.println("            transition: background-color 0.3s ease;");
		out.println("        }");
		out.println("");
		out.println("        .btn:hover {");
		out.println("            background-color: #45a049;");
		out.println("        }");
		out.println("");
		out.println("        /* Animazione per il fade-in della pagina */");
		out.println("        @keyframes fadeIn {");
		out.println("            from {");
		out.println("                opacity: 0;");
		out.println("                transform: translateY(20px);");
		out.println("            }");
		out.println("            to {");
		out.println("                opacity: 1;");
		out.println("                transform: translateY(0);");
		out.println("            }");
		out.println("        }");
		out.println("");
		out.println("    </style>");
		out.println("</head>");
		out.println("<body>");
		out.println("");
		out.println("    <div class=\"container\">");
		out.println("        <h1>Benvenuto nel nostro sito!</h1>");
		out.println("        <p>Ci fa piacere averti qui. Esplora il nostro contenuto e scopri cosa abbiamo preparato per te!</p>");
		out.println("        <a href=\"#\" class=\"btn\" onclick=\"welcomeMessage()\">Clicca per un benvenuto speciale</a>");
		out.println("    </div>");
		out.println("");
		out.println("    <script>");
		out.println("        // Funzione JavaScript per mostrare un messaggio di benvenuto");
		out.println("        function welcomeMessage() {");
		out.println("            alert(\"Grazie per essere entrato! Speriamo che ti piaccia la tua esperienza nel nostro sito.\");");
		out.println("        }");
		out.println("    </script>");
		out.println("");
		out.println("</body>");
		out.println("</html>");
		
		try {
			out.close();
			in.close();
			sock.close();
		} catch (IOException ex) {
		}
		return;
	}
}
