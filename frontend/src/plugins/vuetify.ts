import 'vuetify/styles'
import { createVuetify } from 'vuetify'

export default createVuetify({
  theme: {
    defaultTheme: 'light',
    themes: {
      light: {
        dark: false,
        colors: {
          background: '#F7F6F8',
          surface: '#FFFFFF',
          primary: '#741CE9',
          secondary: '#0F172A',
          error: '#DC2626',
          info: '#0284C7',
          success: '#16A34A',
          warning: '#F59E0B'
        }
      }
    }
  },
  defaults: {
    VBtn: {
      rounded: 'lg'
    },
    VCard: {
      rounded: 'lg',
      elevation: 0
    },
    VTextField: {
      density: 'comfortable',
      variant: 'outlined'
    },
    VSelect: {
      density: 'comfortable',
      variant: 'outlined'
    },
    VAutocomplete: {
      density: 'comfortable',
      variant: 'outlined'
    },
    VTextarea: {
      density: 'comfortable',
      variant: 'outlined'
    }
  }
})
