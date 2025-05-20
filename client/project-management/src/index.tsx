import ReactDOM from 'react-dom/client';
import App from './app/App';
import { AuthProvider } from './shared/context/AuthContext';
import './app/index.scss';


ReactDOM.createRoot(document.getElementById('root')!).render(
    <AuthProvider>
      <App />
    </AuthProvider>
);
