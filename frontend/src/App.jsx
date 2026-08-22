import { BrowserRouter, Routes, Route, Navigate } from "react-router-dom";
import { AuthProvider } from "./context/AuthContext";
import Navbar from "./components/Navbar";
import { ProtectedRoute, AdminRoute } from "./components/ProtectedRoute";

import Login from "./pages/Login";
import Register from "./pages/Register";
import BookList from "./pages/BookList";
import BookDetails from "./pages/BookDetails";
import MyBorrowings from "./pages/MyBorrowings";
import AdminBooks from "./pages/admin/AdminBooks";
import AdminBorrowings from "./pages/admin/AdminBorrowings";
import AdminDashboard from "./pages/admin/AdminDashboard";
import NotFound from "./pages/NotFound";

export default function App() {
  return (
    <BrowserRouter>
      <AuthProvider>
        <Navbar />
        <main>
          <Routes>
            <Route path="/" element={<Navigate to="/books" replace />} />
            <Route path="/login" element={<Login />} />
            <Route path="/register" element={<Register />} />
            <Route path="/books" element={<BookList />} />
            <Route path="/books/:id" element={<BookDetails />} />

            <Route path="/my-borrowings" element={
              <ProtectedRoute><MyBorrowings /></ProtectedRoute>
            } />

            <Route path="/admin/books" element={
              <AdminRoute><AdminBooks /></AdminRoute>
            } />
            <Route path="/admin/borrowings" element={
              <AdminRoute><AdminBorrowings /></AdminRoute>
            } />
            <Route path="/admin/dashboard" element={
              <AdminRoute><AdminDashboard /></AdminRoute>
            } />

            <Route path="*" element={<NotFound />} />
          </Routes>
        </main>
      </AuthProvider>
    </BrowserRouter>
  );
}
